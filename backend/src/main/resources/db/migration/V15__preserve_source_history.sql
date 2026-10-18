-- Keep this migration non-destructive. Historical source records may be useful for
-- auditing and are hidden from the source-library UI by the normal query filter.
-- New move/delete operations remove their own records in the application transaction.
SELECT 1;
