/*
 * Copyright (C) 2015 The Pennsylvania State University and the University of Wisconsin
 * Systems and Internet Infrastructure Security Laboratory
 *
 * Author: Damien Octeau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.psu.cse.siis.ic3.db;

import java.sql.SQLException;
import java.sql.Types;

public class ApplicationTable extends Table {
  private static final String INSERT = "INSERT INTO Applications (app, version,shasum) VALUES (?, ?,?)";
  private static final String FIND = "SELECT id FROM Applications WHERE shasum = ?";

  public int insert(String app, String version, String shasum) throws SQLException {

    int id = find(shasum);
    findStatement.close();
    if (id != NOT_FOUND) {
      return id;
    }

    return forceInsert(app, version, shasum);
  }

  public int forceInsert(String app, String version, String shasum) throws SQLException {
    if (insertStatement == null || insertStatement.isClosed()) {
      insertStatement = getConnection().prepareStatement(INSERT);
    }
    insertStatement.setString(1, app);
    if (version == "") {
      insertStatement.setNull(2, Types.NULL);
    } else {
      insertStatement.setString(2, version);
    }
    
    insertStatement.setString(3, shasum);
    
    if (insertStatement.executeUpdate() == 0) {
      return NOT_FOUND;
    }
    
    return findAutoIncrement();
  }

  public int find(String shasum) throws SQLException {
   
    findStatement = getConnection().prepareStatement(FIND);
    findStatement.setString(1, shasum);
   
    return processIntFindQuery(findStatement);
  }
}
