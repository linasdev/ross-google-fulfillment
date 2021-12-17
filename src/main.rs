#![feature(decl_macro)]

use rocket::fairing::AdHoc;

pub mod controllers;
pub mod routes;
pub mod models;
pub mod errors;

fn main() {
    routes::build().launch();
}
