/*
 *   $Id$
 * 
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef omero_API
#define omero_API

#include <omero.ice>
#include <RTypes.ice>
#include <System.ice>
#include <ROMIO.ice>
#include <Glacier2/Session.ice>
#include <Ice/BuiltinSequences.ice>

module omero { 
  module api {     
  
    ["java:type:java.util.ArrayList"] 
    sequence<omero::model::Experimenter> ExperimenterList;
  
    ["java:type:java.util.ArrayList"] 
    sequence<omero::model::ExperimenterGroup> ExperimenterGroupList;

    ["java:type:java.util.ArrayList"] 
    sequence<omero::model::IObject> IObjectList;

    ["java:type:java.util.ArrayList"] 
    sequence<omero::model::Image> ImageList;

    ["java:type:java.util.ArrayList"] 
    sequence<string> StringSet;

    interface IAdmin
    {
    
      // Getters
      nonmutating omero::model::Experimenter getExperimenter(long id) throws ServerError;
      nonmutating omero::model::Experimenter lookupExperimenter(string name) throws ServerError;
      nonmutating ExperimenterList lookupExperimenters() throws ServerError;
      nonmutating omero::model::ExperimenterGroup getGroup(long id) throws ServerError;
      nonmutating omero::model::ExperimenterGroup lookupGroup(string name) throws ServerError ; 
      nonmutating ExperimenterGroupList lookupGroups() throws ServerError;
      nonmutating ExperimenterList containedExperimenters(long groupId) throws ServerError;
      nonmutating ExperimenterGroupList containedGroups(long experimenterId) throws ServerError;
      nonmutating omero::model::ExperimenterGroup getDefaultGroup(long experimenterId) throws ServerError;
    
      // Mutators
    
      idempotent void updateExperimenter(omero::model::Experimenter experimenter) throws ServerError;
      idempotent void updateGroup(omero::model::ExperimenterGroup group) throws ServerError;
      long createUser(omero::model::Experimenter experimenter, string group) throws ServerError;
      long createSystemUser(omero::model::Experimenter experimenter) throws ServerError;
      long createExperimenter(omero::model::Experimenter user, 
			      omero::model::ExperimenterGroup defaultGroup, ExperimenterGroupList groups) throws ServerError; 
      long createGroup(omero::model::ExperimenterGroup group) throws ServerError;
      idempotent void addGroups(omero::model::Experimenter user, ExperimenterList groups) throws ServerError;
      idempotent void removeGroups(omero::model::Experimenter user, ExperimenterGroupList groups) throws ServerError;
      idempotent void setDefaultGroup(omero::model::Experimenter user, omero::model::ExperimenterGroup group) throws ServerError;
      idempotent void setGroupOwner(omero::model::ExperimenterGroup group, omero::model::Experimenter owner) throws ServerError;
      idempotent void deleteExperimenter(omero::model::Experimenter user) throws ServerError;
      idempotent void changeOwner(omero::model::IObject obj, string omeName) throws ServerError;
      idempotent void changeGroup(omero::model::IObject obj, string omeName) throws ServerError;
      idempotent void changePermissions(omero::model::IObject obj, omero::model::Permissions perms) throws ServerError;
      /* Leaving this non-idempotent, because of the overhead, though technically it is. */
      Ice::BoolSeq unlock(IObjectList objects) throws ServerError;
      
      // UAuth
      idempotent void changePassword(omero::RString newPassword) throws ServerError;
      idempotent void changeUserPassword(string omeName, omero::RString newPassword) throws ServerError;
      idempotent void synchronizeLoginCache() throws ServerError;

      // Security Context
      nonmutating omero::sys::Roles getSecurityRoles() throws ServerError;
      nonmutating omero::sys::EventContext getEventContext() throws ServerError;            
    };  

    interface IConfig
    {
      nonmutating string getVersion() throws ServerError;
      nonmutating string getConfigValue(string key) throws ServerError;
      idempotent void setConfigValue(string key, string value) throws ServerError;
      nonmutating omero::Time getDatabaseTime() throws ServerError;
      nonmutating omero::Time getServerTime() throws ServerError;
    };


    interface IPixels
    {
      nonmutating omero::model::Pixels retrievePixDescription(long pixId) throws ServerError;
      nonmutating omero::model::RenderingDef retrieveRndSettings(long pixId) throws ServerError;
      idempotent void saveRndSettings(omero::model::RenderingDef rndSettings) throws ServerError;
      nonmutating int getBitDeptch(omero::model::PixelsType type) throws ServerError;
      nonmutating omero::RObject getEnumeration(string enumClass) throws ServerError;
      nonmutating IObjectList getAllEnumerations(string enumClass) throws ServerError;
    };

    dictionary<long, IObjectList> AnnotationMap;
    dictionary<string, omero::model::Experimenter> UserMap;
    dictionary<int, int> CountMap;

    interface IPojos
    {
      nonmutating IObjectList loadContainerHierarchies(string rootType, Ice::LongSeq rootIds, omero::sys::ParamMap options) throws ServerError;
      nonmutating IObjectList findContainerHierarchies(string rootType, Ice::LongSeq imageIds, omero::sys::ParamMap options) throws ServerError;
      nonmutating AnnotationMap findAnnotations(string rootType, Ice::LongSeq rootIds, Ice::LongSeq annotatorIds, omero::sys::ParamMap options) throws ServerError;
      nonmutating IObjectList findCGCPaths(Ice::LongSeq imageIds, string algo, omero::sys::ParamMap options) throws ServerError;
      nonmutating ImageList findImages(string rootType, Ice::LongSeq rootIds, omero::sys::ParamMap options) throws ServerError;
      nonmutating ImageList findUserImages(omero::sys::ParamMap options) throws ServerError;
      nonmutating UserMap getUserDetails(StringSet names, omero::sys::ParamMap options) throws ServerError;
      nonmutating CountMap getCollectionCount(string type, string property, Ice::LongSeq ids, omero::sys::ParamMap options) throws ServerError;
      nonmutating IObjectList retrieveCollection(omero::model::IObject obj, string collectionName, omero::sys::ParamMap options) throws ServerError;
      omero::model::IObject createDataObject(omero::model::IObject obj, omero::sys::ParamMap options) throws ServerError;
      IObjectList createDataObjects(IObjectList dataObjects, omero::sys::ParamMap options) throws ServerError;
      idempotent void unlink(IObjectList links, omero::sys::ParamMap options) throws ServerError;
      IObjectList link(IObjectList links, omero::sys::ParamMap options) throws ServerError;
      idempotent omero::model::IObject updateDataObject(omero::model::IObject obj, omero::sys::ParamMap options) throws ServerError;
      idempotent IObjectList updateDataObjects(IObjectList objs, omero::sys::ParamMap options) throws ServerError;
      void deleteDataObject(omero::model::IObject obj, omero::sys::ParamMap options) throws ServerError;
      void deleteDataObjects(IObjectList objs, omero::sys::ParamMap options) throws ServerError;
    };

    interface IQuery
    {
      nonmutating omero::model::IObject get(string klass, long id) throws ServerError;
      nonmutating omero::model::IObject find(string klass, long id) throws ServerError;
      nonmutating IObjectList           findAll(string klass, omero::sys::Filter filter) throws ServerError;          
      nonmutating omero::model::IObject findByExample(omero::model::IObject example) throws ServerError;
      nonmutating IObjectList           findAllByExample(omero::model::IObject example, omero::sys::Filter filter) throws ServerError;    
      nonmutating omero::model::IObject findByString(string klass, string field, string value) throws ServerError;
      nonmutating IObjectList           findAllByString(string klass, string field, string value, bool caseSensitive, omero::sys::Filter filter) throws ServerError;
      nonmutating omero::model::IObject findByQuery(string query, omero::sys::Parameters params) throws ServerError;
      nonmutating IObjectList           findAllByQuery(string query, omero::sys::Parameters params) throws ServerError;
      nonmutating omero::model::IObject refresh(omero::model::IObject iObject) throws ServerError;
    };

    interface ITypes
    {
      omero::model::IObject createEnumeration(omero::model::IObject newEnum) throws ServerError;
      nonmutating omero::model::IObject getEnumeration(string type, string value) throws ServerError;
      nonmutating IObjectList allEnumerations(string type) throws ServerError;      
    };

    interface IUpdate
    { 
      void saveObject(omero::model::IObject obj) throws ServerError;
      void saveCollection(IObjectList objs) throws ServerError;     
      omero::model::IObject saveAndReturnObject(omero::model::IObject obj) throws ServerError;
      IObjectList saveAndReturnArray(IObjectList graph) throws ServerError;
      void deleteObject(omero::model::IObject row) throws ServerError;
    };

    interface RawFileStore
    {
      void setFileId(long fileId) throws ServerError;
      nonmutating Ice::ByteSeq read(long position, int length) throws ServerError;
      idempotent void write(Ice::ByteSeq buf, long position, int length) throws ServerError;
    };

    interface RawPixelsStore
    {
      void setPixelsId(long pixelsId) throws ServerError;
      nonmutating int getPlaneSize() throws ServerError;
      nonmutating int getRowSize() throws ServerError;
      nonmutating int getStackSize() throws ServerError;
      nonmutating int getTimepointSize() throws ServerError;
      nonmutating int getTotalSize() throws ServerError;
      nonmutating long getRowOffset(int y, int z, int c, int t) throws ServerError;
      nonmutating long getPlaneOffset(int z, int c, int t) throws ServerError;
      nonmutating long getStackOffset(int c, int t) throws ServerError;
      nonmutating long getTimepointOffset(int t) throws ServerError;
      nonmutating Ice::ByteSeq getRegion(int size, long offset) throws ServerError;
      nonmutating Ice::ByteSeq getRow(int y, int z, int c, int t) throws ServerError;
      nonmutating Ice::ByteSeq getPlane(int z, int c, int t) throws ServerError;
      nonmutating Ice::ByteSeq getStack(int c, int t) throws ServerError;
      nonmutating Ice::ByteSeq getTimepoint(int t) throws ServerError;
      idempotent void setRegion(int size, long offset, Ice::ByteSeq buffer) throws ServerError;
      idempotent void setRow(Ice::ByteSeq buf, int y, int z, int c, int t) throws ServerError;
      idempotent void setPlane(Ice::ByteSeq buf, int z, int c, int t) throws ServerError;
      idempotent void setStack(Ice::ByteSeq buf, int z, int c, int t) throws ServerError;
      idempotent void setTimepoint(Ice::ByteSeq buf, int t) throws ServerError;
      nonmutating Ice::ByteSeq calculateMessageDigest() throws ServerError;
    };

    interface RenderingEngine
    {
      omero::romio::RGBBuffer render(omero::romio::PlaneDef def) throws ServerError;
      Ice::IntSeq renderAsPackedInt(omero::romio::PlaneDef def) throws ServerError;
      void lookupPixels(long pixelsId) throws ServerError;
      bool lookupRenderingDef(long pixelsId) throws ServerError;
      void load() throws ServerError;
      void setModel(omero::model::RenderingModel model) throws ServerError;
      omero::model::RenderingModel getModel() throws ServerError;
      int getDefaultZ() throws ServerError;
      int getDefaultT() throws ServerError;
      void setDefaultZ(int z) throws ServerError;
      void setDefaultT(int t) throws ServerError;
      omero::model::Pixels getPixels() throws ServerError;
      IObjectList getAvailableModels() throws ServerError;
      IObjectList getAvailableFamilies() throws ServerError;
      void setQuantumStrategy(int bitResolution) throws ServerError;
      void setCodomainInterval(int start, int end) throws ServerError;
      omero::model::QuantumDef getQuantumDef() throws ServerError;
      void setQuantizationMap(int w, omero::model::Family fam, double coefficient, bool noiseReduction) throws ServerError;
      omero::model::Family getChannelFamily(int w) throws ServerError;
      bool getchannelNoiseReduction(int w) throws ServerError;
      Ice::DoubleSeq getChannelStats(int w) throws ServerError;
      double getChannelCurveCoefficient(int w) throws ServerError;
      void setChannelWindow(int w, double start, double end) throws ServerError;
      double getChannelWindowStart(int w) throws ServerError;
      double getChannelWindowEnd(int w) throws ServerError;
      void setRGBA(int w, int red, int green, int blue, int alpha) throws ServerError;
      Ice::IntSeq getRGBA(int w) throws ServerError;
      void setActive(int w, bool active) throws ServerError;
      bool isActive(int w) throws ServerError;
      void addCodomainMap(omero::romio::CodomainMapContext mapCtx) throws ServerError;
      void updateCodomainMap(omero::romio::CodomainMapContext mapCtx) throws ServerError;
      void removeCodomainMap(omero::romio::CodomainMapContext mapCtx) throws ServerError;
      void saveCurrentSettings() throws ServerError;
      void resetDefaults() throws ServerError;      
    };

    interface ThumbnailStore 
    {
      void setPixelsId(long pixelsId) throws ServerError;
      void setRenderingDefId(long renderingDefId) throws ServerError;
      Ice::ByteSeq getThumbnail(int sizeX, int sizeY) throws ServerError;
      Ice::ByteSeq getThumbnailByLongsetSide(int size) throws ServerError;
      Ice::ByteSeq getThumbnailDirect(int sizeX, int sizeY) throws ServerError;
      Ice::ByteSeq getThumbnailByLongestSideDirect(int size) throws ServerError;
      void createThumbnails() throws ServerError;
      bool thumbnailExist(int sizeX, int sizeY) throws ServerError;
      void resetDefaults() throws ServerError;
    };

    interface SimpleCallback {
      void call();
    };

    interface ServiceFactory extends Glacier2::Session
    {
      IAdmin*    getAdminService();
      // IAnalysis* getAnalysisService();
      IConfig*   getConfigService();
      // IDelete*   getDeleteService();
      IPixels*   getPixelsService();
      IPojos*    getPojosService();
      IQuery*    getQueryService();
      ITypes*    getTypesService();
      IUpdate*   getUpdateService();
      RawFileStore* createRawFileStore();
      RawPixelsStore* createRawPixelsStore();
      RenderingEngine* createRenderingEngine();
      ThumbnailStore* createThumbnailStore();

      void setCallback(SimpleCallback* callback);
      void close();
    };

  };
};

#endif 
