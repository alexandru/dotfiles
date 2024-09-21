use std::{io::{Error, ErrorKind}, path::PathBuf};

use dns_lookup::{self, lookup_host};
use structopt::StructOpt;

#[derive(StructOpt, Debug)]
#[structopt(name = "basic")]
struct Args {
    #[structopt(name = "CONFIG", parse(from_os_str))]
    config: PathBuf
}

fn read_hosts_from_file(file_path: &PathBuf) -> Result<Vec<(String, Option<String>)>, Error> {
    let content = std::fs::read_to_string(file_path)?;

    content.lines()
        .enumerate()
        .map(|(idx, line)| (idx, line.trim()))
        .filter(|(_idx, line)| !line.is_empty())
        .filter(|(_idx, line)| !line.starts_with('#'))
        .map(|(idx, line)| {
            let mut parts = line.split_whitespace();
            let hostname = parts.next().map(|it| it.to_string()).ok_or(
            Error::new(
                ErrorKind::InvalidData, 
                format!("Missing hostname from line {}", idx)
            ))?;
            let ip = parts.next().map(|it| it.to_string());
            Ok((hostname, ip))
        })
        .collect()
}

fn build_hosts_file(lines: Vec<(String, String)>) -> String {
    let mut hosts = r#"
    ##
    # Host Database
    #
    # localhost is used to configure the loopback interface
    # when the system is booting.  Do not change this entry.
    ##
    127.0.0.1	    localhost
    255.255.255.255	broadcasthost
    ::1             localhost

    ##
    # Cache
    #
    "#.trim_end().to_string();

    let max_len = lines.iter()
        .map(|(hostname, _)| hostname.len())
        .max()
        .unwrap_or(0);

    for (hostname, ip) in lines {
        hosts.push_str(
            &format!("\n{:<width$}  {}", hostname, ip, width = max_len)
        );
    }

    hosts.lines()
        .map(|line| line.trim_start())
        .collect::<Vec<_>>()
        .join("\n")
}

fn run() -> Result<String, Error> {
    let opt = Args::from_args();
    let mut hosts = read_hosts_from_file(&opt.config)?;
    hosts.sort_by_key(|(h, _i)| h.clone());
    let mut lines = Vec::new();
    
    for (hostname, ip) in hosts {
        match ip {
            Some(ip) => {
                lines.push((hostname, ip));
            }
            None => {
                let ips = lookup_host(&hostname)?;
                for ip in ips {
                    lines.push((hostname.to_string(), ip.to_string()));
                }
            }
        }
    }

    Ok(build_hosts_file(lines))
}

fn main() {
    match run() {
        Ok(hosts) => {
            println!("{}", hosts);
        }
        Err(err) => {
            eprintln!("Error: {}", err);
        }
    }
}
