package com.demo.bankexdh.model.store;

import com.demo.bankexdh.model.rest.RegisterData;

import io.realm.Realm;

import static com.demo.bankexdh.utils.Const.DEVICE_NAME;

public class DataBaseHelper {

    private static class InstanceHolder {
        static final DataBaseHelper INSTANCE = new DataBaseHelper();
    }

    public static DataBaseHelper getInstance() {
        return DataBaseHelper.InstanceHolder.INSTANCE;
    }



    public void setEnabled(boolean enabled) {
        try (Realm realm = Realm.getDefaultInstance()) {
            UserModel model = realm.where(UserModel.class).equalTo(UserModel.ID, UserModel.DEFAULT_ID).findFirst();
            if (model != null) {
                realm.executeTransaction(t -> {
                    model.setIsEnabled(enabled);
                    t.copyToRealmOrUpdate(model);
                });
            }
        }
    }

    public boolean isEnabled() {
        try (Realm realm = Realm.getDefaultInstance()) {
            UserModel model = realm.where(UserModel.class).equalTo(UserModel.ID, UserModel.DEFAULT_ID).findFirst();
            if (model != null) {
                return model.getIsEnabled();
            }
        }
        return false;
    }

    public void insertUserModel(RegisterData registerData) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(t -> {
                UserModel model = new UserModel();
                model.setId(UserModel.DEFAULT_ID);
                model.setAccessToken(registerData.getToken().getAccessToken());
                model.setRefreshToken(registerData.getToken().getRefreshToken());
                model.setIsEnabled(true);
                t.copyToRealmOrUpdate(model);
            });
        }
    }


    public void insertDevice(RegisterData registerData) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(t -> {
                String uiid = registerData.getDeviceId();
                DeviceModel newModel = new DeviceModel();
                newModel.setId(DeviceModel.DEFAULT_ID);
                newModel.setDeviceId(uiid);
                String firstPart = uiid.substring(0, uiid.indexOf("-"));
                newModel.setName(String.format(DEVICE_NAME, firstPart));
                t.copyToRealmOrUpdate(newModel);
            });
        }
    }

    public String getToken() {
        try (Realm realm = Realm.getDefaultInstance()) {
            UserModel model = realm.where(UserModel.class).equalTo(UserModel.ID, UserModel.DEFAULT_ID).findFirst();
            return model.getAccessToken();
        }
    }

    public String getDeviceId() {
        try (Realm realm = Realm.getDefaultInstance()) {
            DeviceModel model = realm.where(DeviceModel.class)
                    .equalTo(DeviceModel.ID,
                            DeviceModel.DEFAULT_ID).findFirst();
            if (model == null) {
                return null;
            } else {
                return model.getDeviceId();
            }
        }
    }

    public boolean isDeviceRegistered() {
        try (Realm realm = Realm.getDefaultInstance()) {
            DeviceModel model = realm.where(DeviceModel.class)
                    .equalTo(DeviceModel.ID, DeviceModel.DEFAULT_ID).findFirst();
            return model != null;
        }
    }
}