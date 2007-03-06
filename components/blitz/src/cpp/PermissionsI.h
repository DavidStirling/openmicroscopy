
/*
 *   \$Id\$
 * 
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 * 
 */



// Generated by templates/cpp_objects.vm

 
#include <Permissions.h>
#include <Ice/Config.h>
#include <iostream>
#include <string>
#include <vector>

#ifndef PERMISSIONSI_H
#define PERMISSIONSI_H

namespace omero { namespace model {

class PermissionsI : public Permissions { 

protected:
    ~PermissionsI(); // protected as outlined in docs.

public:

   /**
    * Default no-args constructor which manages the proper "loaded"
    * status of all {@link Collection}s by manually initializing them all
    * to an empty {@link Collection} of the approrpriate type.
    */
    PermissionsI();
    PermissionsI(omero::RLongPtr idPtr, bool isLoaded = false);
    void unload(Ice::Current c);

  
    long getPerm1() {
        return  perm1 ;
    }
    
    void setPerm1(long _perm1) {
        perm1 =  _perm1 ;
         
    }
 
  };
 }
}
#endif // PERMISSIONSI_H
 
