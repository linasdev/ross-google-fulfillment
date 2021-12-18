use rocket::{launch, routes};
use rocket::fairing::AdHoc;

use crate::config::SecurityConfig;

pub mod controller;
pub mod models;
pub mod errors;
pub mod config;

#[launch]
async fn rocket() -> _ {
    let rocket = rocket::build()
        .mount("/", routes![controller::fulfillment]);

    let figment = rocket.figment();
    let _security_config: SecurityConfig = figment.extract().unwrap();

    rocket.attach(AdHoc::config::<SecurityConfig>())
}
