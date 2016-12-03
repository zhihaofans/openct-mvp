package cc.metapro.openct.university.Library;


import cc.metapro.openct.university.Library.LibraryInterface.LibraryLogin;
import cc.metapro.openct.university.Library.LibraryInterface.LibraryParser;
import cc.metapro.openct.university.Library.LibraryInterface.LibrarySearcher;
import cc.metapro.openct.university.Library.LibraryInterface.LibraryUserCenter;
import cc.metapro.openct.university.URLGenerator;

/**
 * Created by jeffrey on 11/23/16.
 */

public abstract class UniversityLibrary implements URLGenerator, LibrarySearcher, LibraryLogin, LibraryUserCenter, LibraryParser {

}
