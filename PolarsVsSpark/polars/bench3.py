import polars as pl
from datetime import datetime


def read_parquet(path: str) -> None:
    return pl.read_parquet(path)


def main():
    t1 = datetime.now()

    read_path = 'root/parquets/parquets/*.parquet'

    df = read_parquet(read_path)
    cnt = df.filter(pl.col("model") == "value").count()
    
    #cnt = pl.scan_parquet(read_path).select(pl.count()).collect()
    t2 = datetime.now()

    print(f"Time to run polars pipeline : {t2 - t1}")
    print(cnt)


if __name__ == "__main__":
    main()
