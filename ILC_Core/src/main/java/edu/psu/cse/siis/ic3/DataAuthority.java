//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.psu.cse.siis.ic3;

import java.util.Objects;

public class DataAuthority {
    private final String host;
    private final String port;

    public DataAuthority(String host, String port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return this.host;
    }

    public String getPort() {
        return this.port;
    }

    public boolean isPrecise() {
        return !"(.*)".equals(this.host) && !"(.*)".equals(this.port);
    }

    public String toString() {
        return "host " + this.host + ", port " + this.port;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.host, this.port});
    }

    public boolean equals(Object other) {
        if(!(other instanceof DataAuthority)) {
            return false;
        } else {
            DataAuthority secondDataAuthority = (DataAuthority)other;
            return Objects.equals(this.host, secondDataAuthority.host) && Objects.equals(this.port, secondDataAuthority.port);
        }
    }
}
