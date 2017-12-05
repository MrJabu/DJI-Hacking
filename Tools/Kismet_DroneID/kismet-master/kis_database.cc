/*
    This file is part of Kismet

    Kismet is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Kismet is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Kismet; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

#include "kis_database.h"
#include "configfile.h"
#include "messagebus.h"
#include "globalregistry.h"
#include "util.h"

KisDatabase::KisDatabase(GlobalRegistry *in_globalreg, std::string in_module_name) :
        ds_module_name(in_module_name) {

    globalreg = in_globalreg;

}

KisDatabase::~KisDatabase() {
    local_eol_locker lock(&ds_mutex);

    if (db != NULL) {
        sqlite3_close(db);
        db = NULL;
    }
}

bool KisDatabase::Database_Open(std::string in_file_path) {
    char *sErrMsg = NULL;

    if (in_file_path.length() == 0) {
        std::string config_dir_path_raw = 
            globalreg->kismet_config->FetchOpt("configdir");
        std::string config_dir_path =
            globalreg->kismet_config->ExpandLogPath(config_dir_path_raw, "", "", 0, 1);

        ds_dbfile = config_dir_path + "/" + ds_module_name + ".db3"; 
    } else {
        ds_dbfile = in_file_path;
    }

    local_locker lock(&ds_mutex);

    int r;

    r = sqlite3_open(ds_dbfile.c_str(), &db);

    if (r) {
        _MSG("KisDatabase unable to open file " + ds_dbfile + ": " +
                std::string(sqlite3_errmsg(db)), MSGFLAG_ERROR);
        db = NULL;
        return false;
    }

    // Do we have a KISMET table?  If not, this is probably a new database.
    bool k_t_exists = false;

    std::string sql = 
        "SELECT name FROM sqlite_master WHERE type='table' AND name='KISMET'";

    // If the callback is called, we've matched a table name, so we exist
    r = sqlite3_exec(db, sql.c_str(), 
            [] (void *aux, int, char **, char **) -> int {
                bool *m = (bool *) aux;
                *m = true;
                return 0;
            }, 
            (void *) &k_t_exists, &sErrMsg);

    if (r != SQLITE_OK) {
        _MSG("KisDatabase unable to query for KISMET master table in " + ds_dbfile + ": " + 
                std::string(sErrMsg), MSGFLAG_ERROR);
        sqlite3_close(db);
        db = NULL;
        return false;
    }

    // If the table doesn't exist, build it...
    if (!k_t_exists) {
        // Build the master table
        if (!Database_CreateMasterTable())
            return false;
    }

    return true;
}

bool KisDatabase::Database_CreateMasterTable() {
    local_locker dblock(&ds_mutex);

    std::string sql;

    int r;
    char *sErrMsg = NULL;
    sqlite3_stmt *stmt = NULL;
    const char *pz = NULL;

    sql = 
        "CREATE TABLE KISMET ("
        "kismet_version TEXT, "
        "db_version INT, "
        "db_module TEXT)";

    r = sqlite3_exec(db, sql.c_str(),
            [] (void *, int, char **, char **) -> int { return 0; }, NULL, &sErrMsg);

    if (r != SQLITE_OK) {
        _MSG("KisDatabase unable to create KISMET master table in " + ds_dbfile + ": " +
                std::string(sErrMsg), MSGFLAG_ERROR);
        sqlite3_close(db);
        db = NULL;
        return false;
    }

    sql = 
        "INSERT INTO KISMET (kismet_version, db_version, db_module) VALUES (?, ?, ?)";

    r = sqlite3_prepare(db, sql.c_str(), sql.length(), &stmt, &pz);

    if (r != SQLITE_OK) {
        _MSG("KisDatabase unable to generate prepared statement for master table in " +
                ds_dbfile + ": " + string(sqlite3_errmsg(db)), MSGFLAG_ERROR);
        sqlite3_close(db);
        db = NULL;
        return false;
    }

    std::string kversion = globalreg->version_major + "." + 
        globalreg->version_minor + "." +
        globalreg->version_tiny;

    sqlite3_bind_text(stmt, 1, kversion.c_str(), kversion.length(), 0);
    sqlite3_bind_int(stmt, 2, 0);
    sqlite3_bind_text(stmt, 3, ds_module_name.c_str(), ds_module_name.length(), 0);

    sqlite3_step(stmt);
    sqlite3_finalize(stmt);

    return true;
}

bool KisDatabase::Database_Valid() {
    local_locker dblock(&ds_mutex);

    return (db != NULL);
}

unsigned int KisDatabase::Database_GetDBVersion() {
    local_locker dblock(&ds_mutex);

    if (db == NULL)
        return 0;

    unsigned int v = 0;
    int r;
    char *sErrMsg = NULL;

    std::string sql = 
        "SELECT db_version FROM KISMET";

    r = sqlite3_exec(db, sql.c_str(),
            [] (void *ver, int argc, char **data, char **) -> int {
                if (argc != 1)
                    *((unsigned int *) ver) = 0;

                if (sscanf(data[0], "%u", (unsigned int *) ver) != 1) {
                    *((unsigned int *) ver) = 0;
                }

                return 0; 
            }, &v, &sErrMsg);

    if (r != SQLITE_OK) {
        _MSG("KisDatabase unable to query db_version in" + ds_dbfile + ": " +
                std::string(sErrMsg), MSGFLAG_ERROR);
        sqlite3_close(db);
        db = NULL;
        return 0;
    }

    return v;
}

bool KisDatabase::Database_SetDBVersion(unsigned int version) {
    local_locker dblock(&ds_mutex);

    if (db == NULL)
        return 0;

    int r;
    sqlite3_stmt *stmt = NULL;
    const char *pz = NULL;

    std::string sql;

    sql = 
        "UPDATE KISMET SET kismet_version = ?, db_version = ?";

    r = sqlite3_prepare(db, sql.c_str(), sql.length(), &stmt, &pz);

    if (r != SQLITE_OK) {
        _MSG("KisDatabase unable to generate prepared statement to update master table in " +
                ds_dbfile + ":" + string(sqlite3_errmsg(db)), MSGFLAG_ERROR);
        sqlite3_close(db);
        db = NULL;
        return false;
    }

    std::string kversion = globalreg->version_major + "." + 
        globalreg->version_minor + "." +
        globalreg->version_tiny;

    sqlite3_bind_text(stmt, 1, kversion.c_str(), kversion.length(), 0);
    sqlite3_bind_int(stmt, 2, version);

    sqlite3_step(stmt);
    sqlite3_finalize(stmt);

    return true;
}

