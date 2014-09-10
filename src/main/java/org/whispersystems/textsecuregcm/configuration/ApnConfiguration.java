/**
 * Copyright (C) 2013 Open WhisperSystems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.textsecuregcm.configuration;

import java.io.File;
import java.io.FileInputStream;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApnConfiguration {
  @JsonProperty
  private String certpath;
  @JsonProperty
  private String keypath;
  
  private String certificate;

  private String key;

  public String getCertificate() {
      if ( certificate == null)
      {
          certificate = readfromfile(certpath);
      }
    return certificate;
  }

  public String getKey() {
      if ( key == null)
      {
          key = readfromfile(keypath);
      }
    return key;
  }

public String getCertpath() {
    return certpath;
}

public String getKeypath() {
    return keypath;
}
  private String readfromfile(String path)
  {
    try {
      FileInputStream fis=new FileInputStream(new File(path));
      byte[] b=new byte[fis.available()];
      fis.read(b);
      fis.close();
      return new String(b);
      
    } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    return null;
  }
}
