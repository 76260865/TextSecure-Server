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
package org.whispersystems.textsecuregcm.util;

import org.whispersystems.textsecuregcm.configuration.SwiftConfiguration;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlSigner {
  private String host;
  private String imagepath;
  private String secret;
  private static final long   DURATION = 60 * 60 * 1000;

  public UrlSigner(SwiftConfiguration config) {
	  this.host = config.getHost();
	  this.imagepath = config.getImagepath();
	  this.secret = config.getSecret();
  }

  public URL getPreSignedUrl(Long attachmentId, String method){
	  Long epoch = System.currentTimeMillis()/1000+DURATION;
	  try {
		return new URL(host+imagepath+attachmentId.toString()+"?temp_url_sig="+HMACSHA1.HmacSHA1Encrypt(method+"\n"+epoch.toString()+"\n"+imagepath+attachmentId.toString(), secret)+"&temp_url_expires="+epoch.toString());
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  return null;
  }

}
