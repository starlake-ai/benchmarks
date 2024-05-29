import polars as pl
from datetime import datetime


def read_parquet(path: str) -> None:
    return pl.scan_parquet(path)


def main():
    t1 = datetime.now()

    read_path = 'root/parquets/parquets/*.parquet'

    df = read_parquet(read_path)
    filtered_df = df.filter(pl.col("model") == "value")

    cnt = filtered_df.select(pl.len()).collect()
    t2 = datetime.now()
    print(cnt)
    print(f"Time to run polars pipeline : {t2 - t1}")


if __name__ == "__main__":
    main()
