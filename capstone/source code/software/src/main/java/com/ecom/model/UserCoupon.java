package com.ecom.model;

import java.sql.Date;



import jakarta.persistence.*;

@Entity
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UserDtls userDtls;  // Reference to User

    @ManyToOne
    private Coupon coupon;  // Reference to Coupon

    @Column(nullable = false)
    private Date usedAt;

	public Long getId() {
		return id;
	}

	public UserDtls getUserDtls() {
		return userDtls;
	}

	public void setUserDtls(UserDtls userDtls) {
		this.userDtls = userDtls;
	}

	public void setId(Long id) {
		this.id = id;
	}

	



	public Coupon getCoupon() {
		return coupon;
	}

	public void setCoupon(Coupon coupon) {
		this.coupon = coupon;
	}

	public Date getUsedAt() {
		return usedAt;
	}

	public void setUsedAt(Date usedAt) {
		this.usedAt = usedAt;
	}  
    
    
    // The time when the coupon was used
}
