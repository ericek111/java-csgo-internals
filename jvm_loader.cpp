#include <jni.h>
#include <link.h>
#include <thread>
#include <iostream>

JavaVM* jvm = NULL;
// just to be thread-safe we can use this:
// static thread_local jcheatthread jc = {};
// if (jvm && jc.jvm == NULL) initJVM(jc);

struct jcheatthread {
	JNIEnv* env = NULL;
	//jobject jengine = NULL;
} mainjvm;
void initJVM(jcheatthread &jc);
void attachJVMThread(JavaVM* jvm, JNIEnv** env);

int jvm_entry() {
	initJVM(mainjvm);
	return 0;
}
void attachJVMThread(JavaVM* jvm, JNIEnv** env) {
	int res = jvm->GetEnv((void**) env, JNI_VERSION_1_6);
	if(res != JNI_OK){
		// or AttachCurrentThreadInternal(jvm, envp, args, JNI_TRUE);
		int res2;
		//if (daemon) // < Does this even work?
		//	res2 = jvm->AttachCurrentThreadAsDaemon((void**) env, NULL);
		//else
			res2 = jvm->AttachCurrentThread((void**) env, NULL);
		if(res2 != JNI_OK) {
			std::cerr << "FAILED: AttachCurrentThread " << res2 << " GetEnv: " << res << std::endl;
			return;
		}
	}
}
void initJVM(jcheatthread &jc) {
	jmethodID jengineHello = NULL;
	jmethodID jengineBhop = NULL;
	JNIEnv* env = jc.env;
	jobject jengine = NULL;
	bool firstrun = false;

	if(jvm == NULL) {
		firstrun = true;
		JavaVMOption options[] = {
		  //{ const_cast<char*>("-verbose:gc"), NULL },
		  //{ const_cast<char*>("-verbose:class"), NULL },
		  //{ const_cast<char*>("-verbose:jni"), NULL },
	
      { const_cast<char*>("-Djava.class.path=" JAVA_CP), NULL },
      { const_cast<char*>("-agentlib:jdwp=transport=dt_socket,address=localhost:8000,server=y,suspend=n"), NULL },
		};

		JavaVMInitArgs vm_args;
		vm_args.version = JNI_VERSION_1_6;
		vm_args.options = options;
		vm_args.nOptions = sizeof(options) / sizeof(JavaVMOption);
		std::cout << "Creating JVM..." << std::endl;
		int res = JNI_CreateJavaVM(&jvm, reinterpret_cast<void**>(&env), &vm_args);
		if (res == JNI_EEXIST) {
			jint count = 0;
			JavaVM** buffer;
			JNI_GetCreatedJavaVMs(buffer, 1, &count);
			jvm = *buffer;

			attachJVMThread(jvm, &env);
			
			/*std::cout << "Destroying..." << std::endl;
			jvm->DestroyJavaVM();

			std::cout << "Starting new..." << std::endl;
			JNI_CreateJavaVM(&jvm, reinterpret_cast<void**>(&env), &vm_args);*/
			std::cout << "Reattached JVM." << std::endl;
		} else if (res != JNI_OK) {
			std::cerr << "FAILED: JNI_CreateJavaVM " << res << std::endl;
			return;
		}
	} else if(env == NULL) {
		attachJVMThread(jvm, &env);
	}
  jclass mainCls = env->FindClass("eu/lixko/csgointernals/Main");
  if (mainCls == NULL) {
    std::cerr << "FAILED: FindClass" << std::endl;
    return;
  }

  if(firstrun) {
	  jmethodID mid = env->GetStaticMethodID(mainCls, "main", "([Ljava/lang/String;)V");
	  if (mid == NULL) {
	    std::cerr << "FAILED: GetStaticMethodID" << std::endl;
	    return;
	  }

	  jclass string_cls = env->FindClass("java/lang/String");
	  jobject initial_element = NULL;
	  jobjectArray method_args = env->NewObjectArray(0, string_cls, NULL); // initial_element = NULL
	  env->CallStaticVoidMethod(mainCls, mid, method_args);
  }
  jc.env = env;

  // in case you want to access the Engine object in C++ natively
  /*
  jfieldID fid = env->GetStaticFieldID(mainCls, "engine", "Leu/lixko/csgointernals/Engine;");
  if(fid == NULL) {
    std::cout << "Couldn't find engine in Main!" << std::endl;
    return;
  }

  jengine = env->GetStaticObjectField(mainCls, fid);
  if(env->IsSameObject(jengine, NULL)) {
    std::cout << "jengine is null!" << std::endl;
    return;
  }
  jclass jengineCls = env->GetObjectClass(jengine);
  
  jc.jengine = jengine;
  */
}
void __attribute__ ((constructor)) entry() {
	std::thread init_thread(jvm_entry);
	init_thread.detach();
}
void __attribute__  ((destructor)) finish(void) {
	return;
	// TODO: Unloading - not possible due to JDK-4093633
}