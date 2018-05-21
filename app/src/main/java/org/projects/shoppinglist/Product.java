package org.projects.shoppinglist;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
    String name;
    String variant;
    int quantity;

    public Product() {
        // Empty constructor.
    }

    public Product(String name, int quantity, String variant) {
        this.name = name;
        this.variant = variant;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return name + " " + quantity + " " + variant;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(variant);
        dest.writeInt(quantity);
    }

    // Creator
    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    // "De-parcel object
    public Product(Parcel in) {
        name = in.readString();
        variant = in.readString();
        quantity = in.readInt();
    }

    // Get
    public int getQuantity() {
        return quantity;
    }

    public String getName() {
        return name;
    }

    public String getVariant() {
        return variant;
    }

    // Set
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }
}
