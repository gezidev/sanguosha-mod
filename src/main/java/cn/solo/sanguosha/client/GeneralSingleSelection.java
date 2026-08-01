package cn.solo.sanguosha.client;

import java.util.Set;

public final class GeneralSingleSelection {
    private String selectedGeneral;
    private boolean pending;
    private boolean confirmed;

    public String selectedGeneral() {
        return this.selectedGeneral;
    }

    public boolean pending() {
        return this.pending;
    }

    public boolean confirmed() {
        return this.confirmed;
    }

    public boolean canConfirm() {
        return this.selectedGeneral != null && !this.pending && !this.confirmed;
    }

    public void click(String generalId) {
        if (this.pending || this.confirmed) {
            return;
        }
        this.selectedGeneral = generalId.equals(this.selectedGeneral) ? null : generalId;
    }

    public String beginConfirmation() {
        if (!this.canConfirm()) {
            return null;
        }
        this.pending = true;
        return this.selectedGeneral;
    }

    public void applyResult(boolean success) {
        this.pending = false;
        this.confirmed = success;
    }

    public void retainOnly(Set<String> offered) {
        if (!(this.pending || this.confirmed || this.selectedGeneral == null || offered.contains(this.selectedGeneral))) {
            this.selectedGeneral = null;
        }
    }
}

