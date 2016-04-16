package com.smartpocket.timeline.model;

public class Picture {
    private Data data;

    @Override
    public String toString() {
        return "Picture{" +
                "data=" + data +
                '}';
    }

    public Picture() {
    }

    public Picture(Data data) {

        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Picture picture = (Picture) o;

        return data != null ? data.equals(picture.data) : picture.data == null;

    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    public Data getData() {

        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public class Data {
        private String url;

        public Data() { }

        public Data(String url) {
            this.url = url;
        }

        public String getUrl() {

            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUrlHighRes() {
            String result = url;
            if (url != null) {
                result = url.replace("p50x50", "p200x200");
            }
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Data data = (Data) o;
            return url != null ? url.equals(data.url) : data.url == null;

        }

        @Override
        public int hashCode() {
            return url != null ? url.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "url='" + url + '\'' +
                    '}';
        }
    }
}
