import React, { useContext } from 'react'

import './Payment.css'
import { StoreContext } from '../../Context/StoreContext'
const Payment = () => {
  const{cartItems,food_list,removeFromCart ,getTotalCartAmount} = useContext(StoreContext);

  return (
    <div className='payment'>
      <div className="window">
        <div className="payment-order-info">
          <div className="payment-order-info-content">
            <h2>Order Summary</h2>
            <hr />
            <div className="payment-cart-items">
        
        <br />
        {food_list.map((item,index)=>{
          if(cartItems[item._id]>0)
          {
            return (
              <div>
                <div className="payment-cart-items-title payment-cart-items-item">
                  <img src={item.image} alt="" />
                  <p className='name'>{item.name} <br /> <span>Quantity:{cartItems[item._id]}</span>  </p>
                  <p className='price'>${item.price*cartItems[item._id]}</p>
                </div>
              </div>
              
            )
          }
        })}
      </div>
      <div className="payment-cart-total cart-total">
          {/* <div>
            <div className="payment-cart-total-details cart-total-details">
              <p>Subtotal</p>
              <p>${getTotalCartAmount()}</p>
            </div>
            <div className="payment-cart-total-details cart-total-details">
              <p>Delivery Fee</p>
              <p>${ getTotalCartAmount()===0?0:2}</p>
            </div>

            <div className="payment-cart-total-details cart-total-details">
              <b>Total</b>
              <b>${getTotalCartAmount()===0?0:getTotalCartAmount()+2}</b>
            </div>
          </div> */}
          <div>
            <div className="cart-total-details">
              <p>Subtotal</p>
              <p>${getTotalCartAmount()}</p>
            </div>
            <hr />
            <div className="cart-total-details">
              <p>Tax (10%)</p>
              <p>${ getTotalCartAmount()===0?0:getTotalCartAmount()*0.1}</p>
            </div>
            <hr />
            <div className="cart-total-details">
              <p>Delivery Fee</p>
              <p>${ getTotalCartAmount()===0?0:2}</p>
            </div>
            <hr />
            
            <div className="cart-total-details">
              <b>Total</b>
              <b>${getTotalCartAmount()===0?0:getTotalCartAmount()+getTotalCartAmount()*0.1+2}</b>
            </div>
          </div>
        </div>
          </div>
        </div>
        <div class='credit-info'>
      <div class='credit-info-content'>
        
        <img src='https://dl.dropboxusercontent.com/s/2vbqk5lcpi7hjoc/MasterCard_Logo.svg.png' height='80' class='credit-card-image' id='credit-card-image'></img>
        Card Number
        <input class='input-field'></input>
        Card Holder
        <input class='input-field'></input>
        <table class='half-input-table'>
          <tr>
            <td> Expires
              <input class='input-field'></input>
            </td>
            <td>CVC
              <input class='input-field'></input>
            </td>
          </tr>
        </table>
        <button class='pay-btn'>Checkout</button>

      </div>

    </div>
  </div>
</div>

  )
}

export default Payment
