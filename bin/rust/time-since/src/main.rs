use chrono::{DateTime, NaiveDate, NaiveDateTime, Utc};
use structopt::StructOpt;

#[derive(Debug, StructOpt)]
struct Args {
    /// Format: `yyyy-mm-dd` or `yyyy-mm-ddTHH:MM:SS`.
    #[structopt(parse(try_from_str = parse_datetime))]
    since: DateTime<Utc>,

    /// Format: `yyyy-mm-dd` or `yyyy-mm-ddTHH:MM:SS`. Defaults to NOW.
    #[structopt(short = "u", long = "until", parse(try_from_str = parse_datetime))]
    until: Option<DateTime<Utc>>,
}

fn parse_datetime(datetime_str: &str) -> Result<DateTime<Utc>, &str> {
    if let Ok(naive_datetime) = NaiveDateTime::parse_from_str(datetime_str, "%Y-%m-%dT%H:%M:%S") {
        Ok(DateTime::<Utc>::from_naive_utc_and_offset(naive_datetime, Utc))
    } else if let Ok(naive_date) = NaiveDate::parse_from_str(datetime_str, "%Y-%m-%d") {
        let naive_datetime = naive_date.and_hms_opt(0, 0, 0).unwrap();
        Ok(DateTime::<Utc>::from_naive_utc_and_offset(naive_datetime, Utc))
    } else {
        Err("not a valid datetime format")
    }
}

fn main() {
    let args = Args::from_args();

    println!("\nSince:   {}", args.since);
    println!("Until:   {}", args.until.map_or("now".to_string(), |x| x.to_string()));

    let since = args.since;
    let until = args.until.unwrap_or_else(Utc::now);

    let duration = until.signed_duration_since(since);
    let days = duration.num_days();
    let hours = duration.num_hours() - days * 24;
    let minutes = duration.num_minutes() - (days * 24 * 60 + hours * 60);
    let seconds = duration.num_seconds() - (days * 24 * 60 * 60 + hours * 60 * 60 + minutes * 60);

    println!(
        "\nElapsed: {} days, {} hours, {} minutes, {} seconds",
        days, hours, minutes, seconds
    );

    let total_seconds = duration.num_seconds() as f64;
    let total_minutes = total_seconds / 60.0;
    let total_hours = total_minutes / 60.0;
    let total_days = total_hours / 24.0;
    let total_weeks = total_days / 7.0;
    let total_months = total_days / 30.417;
    let total_years = total_days / 365.0;

    println!();
    println!("Years:   {:11.2}", total_years);
    println!("Months:  {:11.2}", total_months);
    println!("Weeks:   {:11.2}", total_weeks);
    println!("Days:    {:11.2}", total_days);
    println!("Hours:   {:11.2}", total_hours);
    println!("Minutes: {:11.2}", total_minutes);
    println!();
}
