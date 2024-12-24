package com.ecom.exceptions;

//CouponExpiredException.java
public class CouponExpiredException extends RuntimeException {
public CouponExpiredException(String message) {
 super(message);
}
}