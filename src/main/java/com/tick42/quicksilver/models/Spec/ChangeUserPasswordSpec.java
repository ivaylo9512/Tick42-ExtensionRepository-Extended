package com.tick42.quicksilver.models.Spec;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ChangeUserPasswordSpec {

    @NotNull
    private String currentPassword;

    @NotNull

    private String newPassword;

    @NotNull

    private String repeatNewPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getRepeatNewPassword() {
        return repeatNewPassword;
    }

    public void setRepeatNewPassword(String repeatNewPassword) {
        this.repeatNewPassword = repeatNewPassword;
    }
}
