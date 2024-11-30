import React from 'react'
import './Header.css'
const Header = () => {
  return (
    <div className='header'>
      <div className="header-contents">
        <h2>Order your favourite food here</h2>
        <p>Choose from a diverse menu featured a delectable array of dishes crafted with the finest ingredients and culinary expertise </p>
        <a href="#explore-menu"> <button>View Menu</button></a>
      </div>
    </div>
  )
}

export default Header
