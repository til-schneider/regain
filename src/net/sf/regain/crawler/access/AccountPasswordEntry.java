/*
 * regain - A file search engine providing plenty of formats
 * Copyright (C) 2004-2008  Til Schneider, Thomas Tesche
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Thomas Tesche, regain@thtesche.com
 */
package net.sf.regain.crawler.access;

/**
 * This class represents a account-password entitiy for authentication purposes.
 *
 * @author Thomas Tesche (thtesche), http://www.thtesche.com/
 */
public class AccountPasswordEntry {

  private String accountName;
  private String password;

  public String getAccountName() {
    return accountName;
  }

  public void setAccountName(String username) {
    this.accountName = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }


}
