CREATE TABLE watchlist
(
  account_id VARCHAR,
  asset      VARCHAR,
  FOREIGN KEY (asset) REFERENCES broker.assets(VALUE),
  PRIMARY KEY (account_id, asset)
);
