-- KEY[1] = stockKey
-- KEY[2] = orderKey
-- ARGV[1] = seckillId
-- ARGV[2] = userId
-- ARGV[3] = orderId
local stockKey = KEYS[1]

local orderKey = KEYS[2]

local seckillId = ARGV[1]

local userId = ARGV[2]

local orderId = ARGV[3]

-- 1、判断库存是否充足
local stock = tonumber(redis.call("GET",stockKey))

if stock == nil or stock <=0 then
    return 1
end

-- 2、判断用户是否重复下单
if redis.call("SISMEMBER",orderKey,userId) == 1 then
    return 2
end

-- 3、扣减库存、创建订单
redis.call("DECR",stockKey)
redis.call("SADD",orderKey,userId)

redis.call("XADD","stream:orders","*",
            "userId",userId,
            "seckillId",seckillId,
            "orderId",orderId)

return 0
