use rocket::{ Rocket, routes };

use crate::controllers;

pub fn build() -> Rocket {
    rocket::ignite().mount("/", routes![])
}
