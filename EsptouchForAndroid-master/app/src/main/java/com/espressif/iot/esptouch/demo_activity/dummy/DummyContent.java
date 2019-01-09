package com.espressif.iot.esptouch.demo_activity.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    private static final int COUNT = 0;

//    static {
//        // Add some sample items.
//        for (int i = 1; i <= COUNT; i++) {
//            addItem(createDummyItem(i));
//        }
//    }

    private static void addItem( DummyItem item ) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

//    private static DummyItem createDummyItem(int position) {
//        return new DummyItem(String.valueOf(position), "Item " + position, makeDetails(position),"00:00:00:00:00:00");
//    }

    private static String makeDetails( int position ) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public String id;
        public String content;
        public String details;
        public String mac;
        public String status;
        public String deviceType;
        public String shareStatus;

        public String getShareStatus() {
            return shareStatus;
        }

        public void setShareStatus( String shareStatus ) {
            this.shareStatus = shareStatus;
        }

        public DummyItem( String id, String content, String details, String mac, String deviceType ) {
            this.id = id;
            this.content = content;
            this.details = details;
            this.mac = mac;
            this.deviceType = deviceType;
        }

        @Override
        public String toString() {
            return content;
        }

        public void setId( String id ) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public String getDetails() {
            return details;
        }

        public String getMac() {
            return mac;
        }

        public void setStatus( String status ) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public String getId() {
            return id;
        }

        public void setContent( String content ) {
            this.content = content;
        }

        public void setDetails( String details ) {
            this.details = details;
        }

        public void setMac( String mac ) {
            this.mac = mac;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public void setDeviceType( String deviceType ) {
            this.deviceType = deviceType;
        }
    }
}
