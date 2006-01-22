package net.hyperic.sigar;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class ProcUtil {

    private static boolean isClassName(String name) {
        int len = name.length();
        for (int i=0; i<len; i++) {
            char c = name.charAt(i);
            if (!((c == '.') || Character.isLetter(c))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Try to determina classname for java programs
     */
    public static String getJavaMainClass(SigarProxy sigar, long pid)
        throws SigarException {

        String[] args = sigar.getProcArgs(pid);
        for (int i=1; i<args.length; i++) {
            String arg = args[i];
            if (isClassName(arg)) {
                //example: "java:weblogic.Server"
                return arg;
            }
            else if (arg.equals("-jar")) {
                File file = new File(args[i+1]);
                if (!file.isAbsolute()) {
                    try {
                        String cwd =
                            sigar.getProcExe(pid).getCwd();
                        file = new File(cwd + File.separator + file);
                    } catch (SigarException e) {}
                }

                if (file.exists()) {
                    JarFile jar = null;
                    try {
                        jar = new JarFile(file);
                        return
                            jar.getManifest().
                            getMainAttributes().
                            getValue(Attributes.Name.MAIN_CLASS);
                    } catch (IOException e) {
                    } finally {
                        if (jar != null) {
                            try { jar.close(); }
                            catch (IOException e){}
                        }
                    }
                }
                
                return file.toString();
            }
        }

        return null;
    }
}
