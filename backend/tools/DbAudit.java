import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.MessageDigest;
import java.math.BigDecimal;

/** Read-only helper for auditing the local GooShare MySQL data. */
public final class DbAudit {
    private static String property(String yaml, String key) {
        Matcher matcher = Pattern.compile("(?m)^\\s*" + Pattern.quote(key) + "\\s*:\\s*(.+?)\\s*$").matcher(yaml);
        if (!matcher.find()) {
            throw new IllegalStateException("Missing datasource property: " + key);
        }
        return matcher.group(1).trim();
    }

    private static Connection connect() throws Exception {
        String yaml = Files.readString(Path.of("src/main/resources/application.yml"), StandardCharsets.UTF_8);
        String datasource = yaml.substring(yaml.indexOf("datasource:"), yaml.indexOf("servlet:"));
        return DriverManager.getConnection(
                property(datasource, "url"),
                property(datasource, "username"),
                property(datasource, "password")
        );
    }

    public static void main(String[] args) throws Exception {
        try (Connection connection = connect()) {
            if (args.length == 2 && args[0].equals("backup")) {
                backup(connection, Path.of(args[1]));
                return;
            }
            if (args.length == 2 && args[0].equals("apply")) {
                apply(connection, Path.of(args[1]));
                return;
            }
            if (args.length == 1 && args[0].equals("schema-hash")) {
                System.out.println(schemaHash(connection));
                return;
            }
            if (args.length == 2 && args[0].equals("compare-schema")) {
                compareSchema(connection, Path.of(args[1]));
                return;
            }
            if (args.length == 2 && args[0].equals("rows")) {
                printRows(connection, args[1]);
                return;
            }
            if (args.length == 2 && args[0].equals("audit")) {
                printAudit(connection, args[1]);
                return;
            }

            DatabaseMetaData metadata = connection.getMetaData();
            List<String> tables = new ArrayList<>();
            try (ResultSet rs = metadata.getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
            tables.sort(String::compareToIgnoreCase);

            for (String table : tables) {
                long count;
                try (Statement statement = connection.createStatement();
                     ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM `" + table + "`")) {
                    rs.next();
                    count = rs.getLong(1);
                }
                System.out.println("TABLE " + table + " ROWS " + count);
                try (ResultSet rs = metadata.getColumns(connection.getCatalog(), null, table, "%")) {
                    while (rs.next()) {
                        System.out.printf("  COLUMN %s %s(%d) nullable=%s default=%s%n",
                                rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"), rs.getInt("COLUMN_SIZE"),
                                rs.getString("IS_NULLABLE"), rs.getString("COLUMN_DEF"));
                    }
                }
            }
        }
    }

    private static void printRows(Connection connection, String table) throws Exception {
        if (!table.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name");
        }
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM `" + table + "` ORDER BY 1")) {
            ResultSetMetaData metadata = rs.getMetaData();
            for (int column = 1; column <= metadata.getColumnCount(); column++) {
                if (column > 1) System.out.print("\t");
                System.out.print(metadata.getColumnLabel(column));
            }
            System.out.println();
            while (rs.next()) {
                for (int column = 1; column <= metadata.getColumnCount(); column++) {
                    if (column > 1) System.out.print("\t");
                    String name = metadata.getColumnLabel(column);
                    Object value = rs.getObject(column);
                    if (name.equalsIgnoreCase("password")) {
                        value = "***MASKED***";
                    }
                    String text = value == null ? "NULL" : value.toString();
                    System.out.print(text.replace("\t", " ").replace("\r", " ").replace("\n", "\\n"));
                }
                System.out.println();
            }
        }
    }

    private static void printAudit(Connection connection, String name) throws Exception {
        String sql = switch (name) {
            case "category-mismatch" -> """
                    SELECT i.id, i.title, i.category_id, c.name AS category, i.category_item_id,
                           ci.item_name, ci.category_id AS expected_category_id
                    FROM item i
                    LEFT JOIN category c ON c.id=i.category_id
                    LEFT JOIN category_item ci ON ci.id=i.category_item_id
                    WHERE ci.id IS NULL OR c.id IS NULL OR ci.category_id<>i.category_id
                    ORDER BY i.id""";
            case "duplicates" -> """
                    SELECT title, price, seller_id, description, COUNT(*) AS copies, GROUP_CONCAT(id ORDER BY id) AS ids
                    FROM item GROUP BY title,price,seller_id,description HAVING COUNT(*)>1 ORDER BY MIN(id)""";
            case "item-anomalies" -> """
                    SELECT id,title,price,stock,status,brand_id,category_id,category_item_id,
                           like_count,collect_count,browser_count
                    FROM item
                    WHERE price<0 OR stock<0 OR like_count<0 OR collect_count<0 OR browser_count<0
                       OR category_id IS NULL OR category_item_id IS NULL OR seller_id IS NULL
                       OR (stock=0 AND status=1)
                    ORDER BY id""";
            case "orphans" -> """
                    SELECT 'item.seller_id' AS relation, i.id AS row_id, i.seller_id AS missing_id FROM item i LEFT JOIN seller s ON s.id=i.seller_id WHERE s.id IS NULL
                    UNION ALL SELECT 'item.category_id',i.id,i.category_id FROM item i LEFT JOIN category c ON c.id=i.category_id WHERE c.id IS NULL
                    UNION ALL SELECT 'item.category_item_id',i.id,i.category_item_id FROM item i LEFT JOIN category_item ci ON ci.id=i.category_item_id WHERE ci.id IS NULL
                    UNION ALL SELECT 'item.brand_id',i.id,i.brand_id FROM item i LEFT JOIN brand b ON b.id=i.brand_id WHERE i.brand_id IS NOT NULL AND b.id IS NULL
                    UNION ALL SELECT 'seller.user_id',s.id,s.user_id FROM seller s LEFT JOIN user u ON u.id=s.user_id WHERE u.id IS NULL
                    UNION ALL SELECT 'orders.user_id',o.id,o.user_id FROM orders o LEFT JOIN user u ON u.id=o.user_id WHERE u.id IS NULL
                    UNION ALL SELECT 'orders.item_id',o.id,o.item_id FROM orders o LEFT JOIN item i ON i.id=o.item_id WHERE i.id IS NULL
                    UNION ALL SELECT 'orders.seller_id',o.id,o.seller_id FROM orders o LEFT JOIN seller s ON s.id=o.seller_id WHERE s.id IS NULL
                    UNION ALL SELECT 'comment.item_id',c.id,c.item_id FROM comment c LEFT JOIN item i ON i.id=c.item_id WHERE i.id IS NULL
                    UNION ALL SELECT 'comment.user_id',c.id,c.user_id FROM comment c LEFT JOIN user u ON u.id=c.user_id WHERE u.id IS NULL
                    UNION ALL SELECT 'comment.parent_id',c.id,c.parent_id FROM comment c LEFT JOIN comment p ON p.id=c.parent_id WHERE c.parent_id<>0 AND p.id IS NULL
                    UNION ALL SELECT 'coupon_item_rel.coupon_id',r.id,r.coupon_id FROM coupon_item_rel r LEFT JOIN coupon c ON c.id=r.coupon_id WHERE c.id IS NULL
                    UNION ALL SELECT 'coupon_item_rel.item_id',r.id,r.item_id FROM coupon_item_rel r LEFT JOIN item i ON i.id=r.item_id WHERE i.id IS NULL
                    UNION ALL SELECT 'coupon_order.coupon_id',co.id,co.coupon_id FROM coupon_order co LEFT JOIN coupon c ON c.id=co.coupon_id WHERE c.id IS NULL
                    UNION ALL SELECT 'coupon_order.user_id',co.id,co.user_id FROM coupon_order co LEFT JOIN user u ON u.id=co.user_id WHERE u.id IS NULL""";
            case "order-mismatch" -> """
                    SELECT o.id,o.item_id,i.title,o.seller_id AS order_seller,i.seller_id AS item_seller,
                           o.price AS order_price,i.price AS item_price,o.status,o.pay_time,o.finish_time
                    FROM orders o JOIN item i ON i.id=o.item_id
                    WHERE o.seller_id<>i.seller_id OR o.price<>i.price
                       OR (o.status=0 AND (o.pay_time IS NOT NULL OR o.finish_time IS NOT NULL))
                       OR (o.status>0 AND o.pay_time IS NULL)
                    ORDER BY o.id""";
            case "comment-mismatch" -> """
                    SELECT c.id,c.item_id,i.title,c.user_id,c.user_name,u.username AS expected_user_name,
                           c.parent_id,c.reply_user_id,c.reply_user_name,c.is_seller,
                           CASE WHEN s.id IS NULL THEN 0 ELSE 1 END AS expected_is_seller,c.content
                    FROM comment c
                    LEFT JOIN item i ON i.id=c.item_id
                    LEFT JOIN user u ON u.id=c.user_id
                    LEFT JOIN seller s ON s.user_id=c.user_id AND s.id=i.seller_id
                    WHERE c.user_name<>u.username OR c.is_seller<>(s.id IS NOT NULL)
                       OR (c.parent_id<>0 AND NOT EXISTS (SELECT 1 FROM comment p WHERE p.id=c.parent_id AND p.item_id=c.item_id))
                    ORDER BY c.id""";
            case "coupon-anomalies" -> """
                    SELECT id,title,total_stock,remain_stock,start_time,end_time,
                           (SELECT COUNT(*) FROM coupon_order co WHERE co.coupon_id=coupon.id) AS received
                    FROM coupon
                    WHERE remain_stock<0 OR remain_stock>total_stock OR start_time>=end_time
                       OR remain_stock<>total_stock-(SELECT COUNT(*) FROM coupon_order co WHERE co.coupon_id=coupon.id)
                    ORDER BY id""";
            case "reference-duplicates" -> """
                    SELECT 'brand' AS table_name,name AS value,COUNT(*) AS copies,GROUP_CONCAT(id ORDER BY id) AS ids FROM brand GROUP BY name HAVING COUNT(*)>1
                    UNION ALL SELECT 'category',name,COUNT(*),GROUP_CONCAT(id ORDER BY id) FROM category GROUP BY name HAVING COUNT(*)>1
                    UNION ALL SELECT 'category_item',item_name,COUNT(*),GROUP_CONCAT(id ORDER BY id) FROM category_item GROUP BY item_name HAVING COUNT(*)>1""";
            case "duplicate-links" -> """
                    SELECT 'orders.item_id' AS relation_name,id AS row_id,item_id FROM orders WHERE item_id IN (103,104,105,106,108,109,110,111,113,114,115,116,118,119,120,121,123,124,125,126,128,129,130,131,133,134,135,136,138,139,140,141,147)
                    UNION ALL SELECT 'comment.item_id',id,item_id FROM comment WHERE item_id IN (103,104,105,106,108,109,110,111,113,114,115,116,118,119,120,121,123,124,125,126,128,129,130,131,133,134,135,136,138,139,140,141,147)
                    UNION ALL SELECT 'coupon_item_rel.item_id',id,item_id FROM coupon_item_rel WHERE item_id IN (103,104,105,106,108,109,110,111,113,114,115,116,118,119,120,121,123,124,125,126,128,129,130,131,133,134,135,136,138,139,140,141,147)
                    UNION ALL SELECT 'item_like.item_id',id,item_id FROM item_like WHERE item_id IN (103,104,105,106,108,109,110,111,113,114,115,116,118,119,120,121,123,124,125,126,128,129,130,131,133,134,135,136,138,139,140,141,147)""";
            case "agent-readability" -> """
                    SELECT i.id,i.title,
                           CASE
                             WHEN i.description IS NULL OR i.description NOT LIKE '商品名：%' THEN 'description_not_standardized'
                             WHEN i.brand_id IS NULL AND EXISTS (
                                  SELECT 1 FROM brand b WHERE i.title LIKE CONCAT('%',b.name,'%')
                                     OR i.description LIKE CONCAT('%检索词：%',b.name,'%')) THEN 'explicit_brand_missing_id'
                             ELSE 'ok'
                           END AS issue
                    FROM item i
                    WHERE i.description IS NULL OR i.description NOT LIKE '商品名：%'
                       OR (i.brand_id IS NULL AND EXISTS (
                           SELECT 1 FROM brand b WHERE i.title LIKE CONCAT('%',b.name,'%')
                              OR i.description LIKE CONCAT('%检索词：%',b.name,'%')))
                    ORDER BY i.id""";
            case "search-sample" -> """
                    SELECT id,title,price,stock,brand_id,category_id,category_item_id,description
                    FROM item
                    WHERE (title LIKE '%手机%' OR description LIKE '%手机%')
                      AND brand_id=1 AND price<=3000
                    ORDER BY create_time DESC LIMIT 5""";
            default -> throw new IllegalArgumentException("Unknown audit: " + name);
        };
        printQuery(connection, sql);
    }

    private static void printQuery(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            ResultSetMetaData metadata = rs.getMetaData();
            for (int column = 1; column <= metadata.getColumnCount(); column++) {
                if (column > 1) System.out.print("\t");
                System.out.print(metadata.getColumnLabel(column));
            }
            System.out.println();
            while (rs.next()) {
                for (int column = 1; column <= metadata.getColumnCount(); column++) {
                    if (column > 1) System.out.print("\t");
                    Object value = rs.getObject(column);
                    String text = value == null ? "NULL" : value.toString();
                    System.out.print(text.replace("\t", " ").replace("\r", " ").replace("\n", "\\n"));
                }
                System.out.println();
            }
        }
    }

    private static List<String> tables(Connection connection) throws Exception {
        List<String> tables = new ArrayList<>();
        try (ResultSet rs = connection.getMetaData().getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
            while (rs.next()) tables.add(rs.getString("TABLE_NAME"));
        }
        tables.sort(String::compareToIgnoreCase);
        return tables;
    }

    private static String createTableSql(Connection connection, String table) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SHOW CREATE TABLE `" + table + "`")) {
            rs.next();
            return rs.getString(2);
        }
    }

    private static String schemaHash(Connection connection) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (String table : tables(connection)) {
            digest.update(table.getBytes(StandardCharsets.UTF_8));
            digest.update(createTableSql(connection, table).getBytes(StandardCharsets.UTF_8));
        }
        StringBuilder hex = new StringBuilder();
        for (byte value : digest.digest()) hex.append(String.format("%02x", value));
        return hex.toString();
    }

    private static void compareSchema(Connection connection, Path backupPath) throws Exception {
        String backup = Files.readString(backupPath, StandardCharsets.UTF_8).replace("\r\n", "\n");
        int mismatches = 0;
        for (String table : tables(connection)) {
            Pattern pattern = Pattern.compile("(?s)(CREATE TABLE `" + Pattern.quote(table) + "`.*?);\\n\\n");
            Matcher matcher = pattern.matcher(backup);
            if (!matcher.find()) {
                System.out.println("missing_in_backup=" + table);
                mismatches++;
                continue;
            }
            String before = normalizeCreateTable(matcher.group(1));
            String after = normalizeCreateTable(createTableSql(connection, table));
            if (!before.equals(after)) {
                System.out.println("structure_mismatch=" + table);
                mismatches++;
            }
        }
        System.out.println("schema_structure_mismatches=" + mismatches);
    }

    private static String normalizeCreateTable(String ddl) {
        return ddl.replace("\r\n", "\n")
                .replaceAll("AUTO_INCREMENT=\\d+", "AUTO_INCREMENT=<value>")
                .strip();
    }

    private static void backup(Connection connection, Path path) throws Exception {
        if (path.getParent() != null) Files.createDirectories(path.getParent());
        StringBuilder out = new StringBuilder();
        out.append("-- GooShare full schema and data backup\nSET NAMES utf8mb4;\nSET FOREIGN_KEY_CHECKS=0;\n\n");
        for (String table : tables(connection)) {
            out.append("DROP TABLE IF EXISTS `").append(table).append("`;\n")
                    .append(createTableSql(connection, table)).append(";\n\n");
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT * FROM `" + table + "` ORDER BY 1")) {
                ResultSetMetaData metadata = rs.getMetaData();
                while (rs.next()) {
                    out.append("INSERT INTO `").append(table).append("` VALUES (");
                    for (int column = 1; column <= metadata.getColumnCount(); column++) {
                        if (column > 1) out.append(',');
                        out.append(sqlLiteral(rs.getObject(column)));
                    }
                    out.append(");\n");
                }
            }
            out.append('\n');
        }
        out.append("SET FOREIGN_KEY_CHECKS=1;\n");
        Files.writeString(path, out.toString(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println(path.toAbsolutePath() + " bytes=" + Files.size(path));
    }

    private static String sqlLiteral(Object value) {
        if (value == null) return "NULL";
        if (value instanceof byte[] bytes) {
            StringBuilder hex = new StringBuilder("0x");
            for (byte item : bytes) hex.append(String.format("%02x", item));
            return hex.toString();
        }
        if (value instanceof Number || value instanceof Boolean || value instanceof BigDecimal) {
            return value.toString();
        }
        String text = value.toString().replace("T", " ")
                .replace("\\", "\\\\").replace("'", "''")
                .replace("\u0000", "\\0").replace("\r", "\\r").replace("\n", "\\n");
        return "'" + text + "'";
    }

    private static void apply(Connection connection, Path path) throws Exception {
        String content = Files.readString(path, StandardCharsets.UTF_8);
        StringBuilder cleaned = new StringBuilder();
        for (String line : content.split("\\R")) {
            if (!line.stripLeading().startsWith("--")) cleaned.append(line).append('\n');
        }
        connection.setAutoCommit(false);
        int statements = 0;
        try (Statement statement = connection.createStatement()) {
            for (String sql : cleaned.toString().split(";")) {
                if (sql.isBlank()) continue;
                int changed = statement.executeUpdate(sql.trim());
                statements++;
                System.out.println("statement=" + statements + " changed=" + changed);
            }
            connection.commit();
            System.out.println("COMMIT statements=" + statements);
        } catch (Exception exception) {
            connection.rollback();
            System.out.println("ROLLBACK");
            throw exception;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
