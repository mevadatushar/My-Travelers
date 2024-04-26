package com.example.mytravelers.Modal

class UserDataModal {

    var id = ""
    var profile = ""
    var Username = ""
    var mobile = ""
    var address = ""
    var email = ""
    var password = ""
   var UserAdmin = 0

    constructor(id: String, profile: String, Username: String, mobile: String, address: String,email: String, password: String, UserAdmin: Int)
    {
        this.id = id
        this.email = email
        this.profile = profile
        this.password = password
        this.Username = Username
        this.address = address
        this.mobile = mobile
      this.UserAdmin = UserAdmin
    }

   constructor(){

   }

}