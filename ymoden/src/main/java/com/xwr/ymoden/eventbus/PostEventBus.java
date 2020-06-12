package com.xwr.ymoden.eventbus;

import org.greenrobot.eventbus.EventBus;

/**
 *
 */

public class PostEventBus {

    private PostEventBus() {
    }

    public static void post(String str) {
        EventBus.getDefault().post(new MessageEvent(str));
    }
}
