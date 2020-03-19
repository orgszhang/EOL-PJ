package com.ht.enums;

public enum ComponentFactoryEnum {

    D("11D915743"),
    G("11G915743"),
    OTHERS("--请选择--");

    private String value;

    public String getValue() {
        return value;
    }

    ComponentFactoryEnum(String value) {
        this.value = value;
    }


}
