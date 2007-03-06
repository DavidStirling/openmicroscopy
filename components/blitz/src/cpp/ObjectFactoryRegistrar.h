/*
 *   $Id$
 * 
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 * 
 */

#ifndef OBJECTFACTORYREGISTRAR_H
#define OBJECTFACTORYREGISTRAR_H

#include <string>
#include <Ice/Ice.h>
#include <IceUtil/IceUtil.h>

namespace OMERO { 

  class ObjectFactory : public Ice::ObjectFactory {

  public:
    

    ObjectFactory();
    virtual ~ObjectFactory();
    virtual Ice::ObjectPtr create(const std::string& type);
    virtual void destroy();
    void registerObjectFactory(const Ice::CommunicatorPtr ic);
    void conditionalAdd(const std::string& name, Ice::CommunicatorPtr ic, const Ice::ObjectFactoryPtr of);


  };

  typedef IceUtil::Handle<ObjectFactory> ObjectFactoryPtr;

}

#endif

