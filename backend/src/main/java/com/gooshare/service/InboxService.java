package com.gooshare.service;

import com.gooshare.common.Result;

public interface InboxService {
    Result getInbox(Long max, Integer offset);
}
