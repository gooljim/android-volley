package com.android.volley.toolbox;

/**
 * Cookie.java
 * nfhu
 * 2012-4-7涓嬪崍3:29:37
 **/
 class Cookie{
    /**
     * MODE_NEW
     */
    static final byte MODE_NEW = 0;

    /**
     * MODE_NORMAL
     */
    static final byte MODE_NORMAL = 1;

    /**
     * MODE_DELETED
     */
    static final byte MODE_DELETED = 2;

    /**
     * MODE_REPLACED
     */
    static final byte MODE_REPLACED = 3;

    /**
     * domain
     */
    String domain;

    /**
     * path
     */
    String path;

    /**
     * name
     */
    String name;

    /**
     * value
     */
    String value;

    /**
     * expires
     */
    long expires;

    /**
     * lastAcessTime
     */
    long lastAcessTime;

    /**
     * lastUpdateTime
     */
    long lastUpdateTime;

    /**
     * secure
     */
    boolean secure;

    /**
     * mode
     */
    byte mode;

    /**
     */
    Cookie(){
    }

    /**
     * @param defaultDomain defaultDomain
     * @param defaultPath defaultPath
     */
    Cookie(String defaultDomain, String defaultPath){
        domain = defaultDomain;
        path = defaultPath;
        expires = -1L;
    }

    /**
     * <exactMatch>
     * cookie 姣旇緝
     * @param in
     * @return 
     **/
    boolean exactMatch(Cookie in){
        boolean valuesMatch = !((value == null) ^ (in.value == null));
        return domain.equals(in.domain) && path.equals(in.path)
            && name.equals(in.name) && valuesMatch;
    }


    /**
     * <domainMatch>
     **/
    boolean domainMatch(String urlHost){
        if (domain.startsWith(".")){
            if (urlHost.endsWith(domain.substring(1))){
                int len = domain.length();
                int urlLen = urlHost.length();
                if (urlLen > len - 1){
                    return urlHost.charAt(urlLen - len) == '.';
                }else{
                    return true;
                }
            }else{
                return false;
            }
        }else{
            return urlHost.equals(domain);
        }
    }

    /**
     * <pathMatch>
     * @param urlPath urlPath
     **/
    boolean pathMatch(String urlPath){
        if (urlPath.startsWith(path)){
            int len = path.length();
            if (len == 0){
                return false;
            }
            int urlLen = urlPath.length();
            if (path.charAt(len - 1) != '/' && urlLen > len){
                return urlPath.charAt(len) == '/';
            } else{
                return true;
            }
        } else{
            return false;
        }
    }

    /** 
     * <toString>
     * 
     * @return
     * @see Object#toString()
     */
    public String toString(){
        return (new StringBuffer("domain: ")).append(domain).append("; path: ")
            .append(path).append("; name: ").append(name).append("; value: ")
            .append(value).toString();
    }

}
