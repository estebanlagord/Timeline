package com.smartpocket.timeline.model;

import java.util.ArrayList;
import java.util.List;

public class Attachments {
    public Data[] data;

    public Attachments() {
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
