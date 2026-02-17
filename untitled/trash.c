#define PG_DIAG_SEVERITY		      'S'
#define PG_DIAG_SEVERITY_NONLOCALIZED 'V'
#define PG_DIAG_SQLSTATE		      'C'
#define PG_DIAG_MESSAGE_PRIMARY       'M'
#define PG_DIAG_MESSAGE_DETAIL	      'D'
#define PG_DIAG_MESSAGE_HINT	      'H'
#define PG_DIAG_STATEMENT_POSITION    'P'
#define PG_DIAG_INTERNAL_POSITION     'p'
#define PG_DIAG_INTERNAL_QUERY	      'q'
#define PG_DIAG_CONTEXT			      'W'
#define PG_DIAG_SCHEMA_NAME		      's'
#define PG_DIAG_TABLE_NAME		      't'
#define PG_DIAG_COLUMN_NAME		      'c'
#define PG_DIAG_DATATYPE_NAME	      'd'
#define PG_DIAG_CONSTRAINT_NAME       'n'
#define PG_DIAG_SOURCE_FILE		      'F'
#define PG_DIAG_SOURCE_LINE		      'L'
#define PG_DIAG_SOURCE_FUNCTION       'R'

// TODO: do we need it?
char* PQ_dump_PGresult(PGresult* result, char* bb) {

    // self
    bb = put_long(bb, (long) result);

    int nTuples = PQntuples(result);
    bb = put_int(bb, nTuples);

    // n of columns
    int nColumns = PQnfields(result);
    bb = put_int(bb, nColumns);

    // columns
    char* column;
    Oid tableOid;
    int format;
    Oid oid;
    int typeMod;
    for (int i = 0; i < nColumns; i++) {

        oid = PQftype(result, i);
        bb = put_int(bb, oid);

        format = PQfformat(result, i);
        bb = put_int(bb, format);

        tableOid = PQftable(result, i);
        bb = put_int(bb, tableOid);

        typeMod = PQfmod(result, i);
        bb = put_int(bb, typeMod);

        column = PQfname(result, i);
        bb = put_string(bb, column);
    }

    // params
    int nParams = PQnparams(result);
    bb = put_int(bb, nParams);
    for (int i = 0; i < nParams; i++) {
        oid = PQparamtype(result, i);
        bb = put_int(bb, oid);
    }

    return bb;
}
