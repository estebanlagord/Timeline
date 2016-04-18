package com.smartpocket.timeline.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to be used by Gson to parse Facebook's JSON
 */
public class Attachments {
    private Data[] data;
    private final List<String> linkTypes = Arrays.asList("share", "map");

    public Attachments() {
    }

    public String getSharedLink() {
        String result = null;
        if (data != null && data[0] != null) {
            Data firstAttachment = data[0];
            if (firstAttachment.type != null && linkTypes.contains(firstAttachment.type))
                result = firstAttachment.url;
        }
        return result;
    }

    public List<String> getPictureUrls() {
        List<String> result = new ArrayList<String>();
        if (data != null){
            for (Data d : data) {
                if (d != null && d.media != null && d.media.image != null) {
                    result.add(d.media.image.src);
                }

                if (d != null && d.subattachments != null && d.subattachments.data != null){
                    for (Data dataSub : d.subattachments.data) {
                        if (dataSub.media != null && dataSub.media.image != null) {
                            result.add(dataSub.media.image.src);
                        }
                    }
                }
            }
        }
        return result;
    }

    public class Data {
        public Media media;
        public String type;
        public String url;
        public Subattachments subattachments;

        public Data() {
        }

        private class Media {
            public Image image;

            public Media() {
            }

            public class Image {
                public String src;

                public Image() {
                }
            }
        }

        private class Subattachments {
            public Data[] data;

            public Subattachments() {
            }
        }
    }
}
