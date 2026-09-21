package com.project.milo.constant;

public enum CityEnum {
        MIANYANG(1, "绵阳"),
        DEYANG(2, "德阳"),
        GUANGYUAN(3, "广元"),
        LESHAN(4, "乐山"),
        ZIGONG(5, "自贡及其他"),
        YIBIN(6, "宜宾");

        private  Integer code;
        private  String name;

        // 构造方法（必须是私有的，默认也是私有）
        CityEnum(Integer code, String name) {
                this.code = code;
                this.name = name;
        }

        // 提供 getter
        public Integer getCode() {
                return code;
        }

        public String getName() {
                return name;
        }
}
