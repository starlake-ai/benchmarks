# Transpiling from BigQuery to DuckDB (WIP)

JSQLTranspiler supports multiple input SQL dialects and transpile to DuckDB only.
SQLGlot on the other hand target multiple input and output databases.
This comparison is limited to transpilation from BigQuery to DuckDB.

The full set of tests is available at : [JSQLTranspiler tests](https://github.com/starlake-ai/jsqltranspiler/tree/main/src/test/resources/ai/starlake/transpiler)

Below some of the SQL statements where JSQLTranspiler succeeds whereas SQLGlot fails.

## SQL involving date part extraction

```sql
-- Input
SELECT EXTRACT(HOUR FROM DATETIME(2008, 12, 25, 15, 30, 00)) as hour

-- JSQLTranspiler
SELECT EXTRACT(HOUR FROM Cast(MAKE_DATE(2008, 12, 25) + MAKE_TIME(15, 30, 00) AS DATETIME)) AS hour

-- SQLGlot
SELECT EXTRACT(HOUR FROM DATETIME(2008, 12, 25, 15, 30, 00)) AS hour
```


## Invalid transpilation for some function such as time

```sql
-- Input
SELECT
  TIME(15, 30, 00) as time_hms,
  TIME(DATETIME '2008-12-25 15:30:00') AS time_dt,
  TIME(TIMESTAMP '2008-12-25 15:30:00+08', 'America/Los_Angeles') as time_tstz

-- JSQLTranspiler
SELECT
    MAKE_TIME(15, 30, 00) AS time_hms,
    Cast(DATETIME '2008-12-25 15:30:00' AS TIME) AS time_dt,
    Cast(TIMESTAMPTZ '2008-12-25 15:30:00+08' AT TIME ZONE 'America/Los_Angeles' AS TIME) AS time_tstz

-- SQLGlot
SELECT
    MAKE_TIME(15, 30, 00) AS time_hms,
    CAST(CAST('2008-12-25 15:30:00' AS TIMESTAMP) AS TIME) AS time_dt,
    TIME(CAST('2008-12-25 15:30:00+08' AS TIMESTAMPTZ), 'America/Los_Angeles') AS time_tstz
```

## Don't unnest recursively

```sql
-- Input
SELECT t, len, LPAD(t, len) AS padded FROM UNNEST([
  STRUCT<t STRING, len INTEGER>('abc', 5 ),
  ('abc', 2),
  ('例子', 4)
])

-- JSQLTranspiler
SELECT  t
        , len
        , CASE Typeof( t )
            WHEN 'VARCHAR'
                THEN Lpad( t::VARCHAR, len, ' ' )
            END AS padded
FROM (  SELECT Unnest(  [
                            { t:'abc',len:5 }::STRUCT( t STRING,len INTEGER)
                            , ( 'abc', 2 )
                            , ( '例子', 4 )
                        ], recursive => TRUE ) )
;

-- SQLGlot
SELECT t, len, LPAD(t, len) AS padded
FROM UNNEST([STRUCT(t TEXT, len INT)('abc', 5), ('abc', 2), ('例子', 4)])
```

## Seems to write as is when function not transpiled

```sql
-- Input
SELECT
  DATETIME '2008-12-25 15:30:00' as original_date,
  DATETIME_SUB(DATETIME '2008-12-25 15:30:00', INTERVAL 10 MINUTE) as earlier
;

-- JSQLTranspiler
SELECT
    DATETIME '2008-12-25 15:30:00' AS original_date,
    DATE_ADD(DATETIME '2008-12-25 15:30:00', INTERVAL '-10 MINUTE') AS earlier

-- SQLGlot
SELECT
    CAST('2008-12-25 15:30:00' AS TIMESTAMP) AS original_date,
    DATETIME_SUB(CAST('2008-12-25 15:30:00' AS TIMESTAMP), 10, MINUTE) AS earlier
```

## Seems to write as is when function not transpiled (2)
```
-- Input
SELECT TIME_DIFF(CAST('15:30:00' AS TIME), CAST('14:35:00' AS TIME), MINUTE) AS difference

-- JSQLTranspiler
SELECT DATE_DIFF('MINUTE', TIME '14:35:00', TIME '15:30:00') AS difference

-- SQLGlot
SELECT TIME_DIFF(CAST('15:30:00' AS TIME), CAST('14:35:00' AS TIME), MINUTE) AS difference
```

## Implicit cast

```sql
-- Input
SELECT
  DATE_DIFF('2017-12-30', '2014-12-30', YEAR) AS year_diff,
  DATE_DIFF('2017-12-30', '2014-12-30', ISOYEAR) AS isoyear_diff

-- JSQLTranspiler
SELECT
    DATE_DIFF('YEAR', DATE '2014-12-30', DATE '2017-12-30') AS year_diff,
    DATE_DIFF('ISOYEAR', DATE '2014-12-30', DATE '2017-12-30') AS isoyear_diff

-- SQLGlot
SELECT
    DATE_DIFF('YEAR', '2014-12-30', '2017-12-30') AS year_diff,
    DATE_DIFF('ISOYEAR', '2014-12-30', '2017-12-30') AS isoyear_diff
```

## Date parsing and format

```sql
-- Input
SELECT PARSE_DATE('%A %b %e %Y', 'Thursday Dec 25 2008')  AS date

-- JSQLTranspiler
SELECT CAST(strptime('Thursday Dec 25 2008', '%A %b %-d %Y') AS DATE) AS date

-- SQLGlot
SELECT CAST(STRPTIME('Thursday Dec 25 2008', '%A %b %e %Y') AS DATE) AS date
```

## Difference between Timestamp and timestamp tz

```sql
-- Input
select TIMESTAMP '2016-10-18 2:54:11+02:00', TIMESTAMP '2016-10-18 2:54:11'

-- JSQLTranspiler
SELECT  TIMESTAMPTZ '2016-10-18 2:54:11+02:00'
        , TIMESTAMP '2016-10-18 2:54:11'

-- SQLGlot
SELECT CAST('2016-10-18 2:54:11+02:00' AS TIMESTAMPTZ), CAST('2016-10-18 2:54:11' AS TIMESTAMPTZ)
```