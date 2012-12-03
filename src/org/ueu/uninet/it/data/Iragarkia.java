/**
 * Copyright (C) 2012  Udako Euskal Unibertsitatea informatikaria@ueu.org

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.ueu.uninet.it.data;

public class Iragarkia {

    public String egilea;
    public String edukia;
    public String data;
    public String telefonoa;
    public String atala;
    public String url;
    public Boolean irudia = false;
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
        sb.append("*Iragarkia*\n");
        sb.append("egilea:" + this.egilea + "\n");
        sb.append("edukia:" + this.edukia + "\n");
        sb.append("data:" + this.data + "\n");
        sb.append("telefonoa:" + this.telefonoa + "\n");
        sb.append("atala:" + this.atala + "\n");
        sb.append("url:" + this.url + "\n");
        
        return sb.toString();
    }

}
