import polars as pl
from datetime import datetime


def read_csvs(path: str):
    lazy_df = pl.scan_csv(path)
    return lazy_df


def write_parquets(df, path: str) -> None:
    df.sink_parquet(path, compression='snappy')

def main():
    t1 = datetime.now()

    read_path = 'root/input/*.csv'
    write_path = 'root/parquets/parquets2'

    df = read_csvs(read_path)
    write_parquets(df, write_path)

    t2 = datetime.now()
    print(f"Time to run polars pipeline : {t2 - t1}")


if __name__ == "__main__":
    main()
