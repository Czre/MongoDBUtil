package com.czre.mongo.annotations;

public interface RealizeType {


    String getPath();

    public enum List implements RealizeType {
        ArrayList("java.util.ArrayList"), Vector("java.util.Vector"), LinkedList("java.util.LinkedList");


        private String path;

        List(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public enum Map {

        HashMap("java.util.HashMap"), Hashtable("java.util.Hashtable"), TreeMap("java.util.TreeMap");
        private String path;

        Map(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public enum Set {


        HashSet("java.util.HashSet"), TreeSet("java.util.TreeSet");


        private String path;

        Set(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

}
