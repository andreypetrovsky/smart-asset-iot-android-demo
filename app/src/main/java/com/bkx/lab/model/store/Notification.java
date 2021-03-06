package com.bkx.lab.model.store;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Notification extends RealmObject {
    public static final String ID = "id";
    public static final String PARAMS = "params";
    @PrimaryKey
    private Long id;
    private String params;
}
