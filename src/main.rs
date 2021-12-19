use rocket::{launch, routes, Rocket, Build, fairing};
use rocket::fairing::AdHoc;
use rocket_db_pools::{sqlx, Database};
use log::error;

use crate::config::SecurityConfig;

mod controller;
mod models;
mod errors;
mod entities;
mod config;
mod services;

#[derive(Database)]
#[database("ross_google_fulfillment")]
pub struct Db(sqlx::PgPool);

#[launch]
async fn rocket() -> _ {
    fern::Dispatch::new()
        .format(|out, message, record| {
            out.finish(format_args!(
                "{}[{}][{}] {}",
                chrono::Local::now().format("[%Y-%m-%d][%H:%M:%S]"),
                record.target(),
                record.level(),
                message
            ))
        })
        .level(log::LevelFilter::Debug)
        .chain(std::io::stdout())
        .apply().unwrap();

    let rocket = rocket::build()
        .mount("/", routes![controller::fulfillment]);

    let figment = rocket.figment();
    let _security_config: SecurityConfig = figment.extract().unwrap();

    rocket.attach(AdHoc::config::<SecurityConfig>())
        .attach(Db::init())
        .attach(AdHoc::try_on_ignite("SQLx Migrations", run_migrations))
}

async fn run_migrations(rocket: Rocket<Build>) -> fairing::Result {
    match Db::fetch(&rocket) {
        Some(db) => match sqlx::migrate!("./migrations").run(&**db).await {
            Ok(_) => Ok(rocket),
            Err(e) => {
                error!("Failed to initialize SQLx database: {}", e);
                Err(rocket)
            }
        }
        None => Err(rocket),
    }
}
