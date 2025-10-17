import java.lang.reflect.InvocationTargetException;
import java.io.*;
import java.nio.file.*;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.io.IOAccess;
import org.graalvm.python.embedding.*;

import org.apache.commons.io.FileUtils;


public class GraalPy {
    static VirtualFileSystem vfs;

    public static Context createPythonContext(String pythonResourcesDirectory) { // ?
        return GraalPyResources.contextBuilder(Path.of(pythonResourcesDirectory)).build();
    }

    public static Context createPythonContextFromResources() {
        if (vfs == null) { // ?
            vfs = VirtualFileSystem.newBuilder().allowHostIO(VirtualFileSystem.HostIO.READ).build();
        }
        return GraalPyResources.contextBuilder(vfs).build();
    }
  
    public static Context createPythonContextFromVirtualEnvironment(String installDir,String execPath,String workingDir){
		//if (useEnv ==null && System.getProperty("venv") == null) {
        //    throw new Exception("Unable to run python, No virtual environment 'venv' specified in system property.");
        //}
      
        //Path executable = Paths.get((useEnv != null?useEnv:System.getProperty("venv")), "bin", "python");
        Context context = Context.newBuilder("python") // ?
                .allowAllAccess(true)
                .currentWorkingDirectory(Paths.get(workingDir))
                .option("python.PythonPath", installDir)
                .option("python.Executable", execPath) // ?
                .option("python.ForceImportSite", "true") // ?
                //.allowIO(IOAccess.newBuilder().allowHostFileAccess(true).build()) // ?
                .build()
           	return context;
    }
  
    public static Context createPythonContextFromGraalEmbed(){
		String path = System.getProperty("graalpy.resources");
        if (path == null || path.isBlank() || path.equals("null")) {
            throw new Exception("Unable to access python code, 'graalpy.resources' system property.");
        }      
        return GraalPy.createPythonContext(path);
    }
}

public class DocProcessor{
	Object httpRequest;
  	Object httpResponse;
  	Object exec;
  	Map arguments;
  
  	Map pluginContext;
  	Object moduleContext; 
  
    Map config;
    Map session;
      
    Object aiProviderInterface;
  	Object aiImplPlugin;
    String PYTHON_MODE = "virtual-environment";
  
    Map toolDefinition = new HashMap();
  
    public DocProcessor(){

    }
  
    public DocProcessor(Object httpRequest,Object httpResponse,Map arguments,Object exec){
          this.arguments				= arguments;
          this.httpRequest 				= arguments.get("httpRequest");
          this.httpResponse 			= arguments.get("httpResponse");
          this.exec						= exec;
		  this.pluginContext 			= arguments.get("pluginContext");
      	  this.moduleContext 			= arguments.get("moduleContext"); 
            
          exec.logger().info("DocProcessor");
    }  
  
    public DocProcessor setAIContext(Object aiProviderInterface, Object aiImplPlugin){
		this.aiProviderInterface = aiProviderInterface;
  		this.aiImplPlugin = aiImplPlugin;
      	return this;
    }
  
    public Map initEvalContext(Map prompt,Map config,Map session,Object appBuilder,Object aiInterface,Object aiImplPlugin,Object tools){
        //task,aiInterfacePlugin
		/*Map leadSource = 
        exec
        .using(pluginContext.resolveToUserAssetDir("/").getCanonicalPath()+"/work/BusinessTools/web")
        .call("/secure/sales-lead-management/db-access/get_next_new_lead_source.stm");
      
        if(leadSource == null)
       		return ["taskStatus":"no lead source to process"];
      
        for(java.io.File entry :pluginContext.resolveToUserAssetDir("/work/sales-lead-processing").listFiles()){
      		entry.delete();  
        }      
      
        prompt.put("workingDir","/work/sales-lead-processing");
        return [
          leadSource:leadSource
        ];*/
        return new HashMap()
    }

    public Map extract_pdf_text(Map toolCallArgs,Map tool_call,Map prompt,Map config,Map session,Object aiProviderInterface,Object aiImplPlugin){
      
		Map respMsg = [
            role:"tool",
            tool_call_id:tool_call.id,
            name:tool_call.function.name,
            content:"pdf text successfully extracted."
        ];
      
        if(toolCallArgs.filePath == null || toolCallArgs.filePath.isEmpty()){
            respMsg.content = "No valid pdf file path specified";
            return respMsg;
        }
      
        String filePath = toolCallArgs.filePath;
      
        //List leads = String.class.isInstance(toolCallArgs.leads)?exec.add("solvent_javalang_class","map").eval(aiImplPlugin.jsonrepair(toolCallArgs.leads),"json"):toolCallArgs.leads;     
      
        try
        {
            Map fileInfo = aiImplPlugin.getFileInfo(prompt,tool_call,toolCallArgs,[filePath:filePath]);
            if(fileInfo.error != null){
               respMsg.content = "pdf extract file error for file ${filePath}, ${fileInfo.error}".toString();
               return respMsg;
            }

            String workingDir = "/home/ubuntu/aws-workflow";
          	//FileUtils.writeStringToFile(new File(workingDir+"/input.txt"),instruction,"utf-8");
            new File(workingDir+"/output.txt").delete();
          
            File pdfFile = pluginContext.resolveToPlatformAssetDir(fileInfo.filePath);             
          		  
            FileUtils.copyFile(pdfFile,new File(workingDir+"/input.pdf"));
            //if(!pdfFile.getName().equals("input.pdf"))
          	//   new File(workingDir+"/"+pdfFile.getName()).renameTo(new File(workingDir+"/input.pdf"));
          
            //find plugin that handles object related functions && ./invoke_biomni.sh
            Object cliManager = moduleContext.getPlugin(aiProviderInterface,"com.codesolvent.solvent.ide.plugin.tools.cli-manager");

            Map resp = cliManager.CLIManagerAPI().runCLI(
              workingDir,
              false,
              "chmod +x invoke_workflow.sh && ./invoke_workflow.sh input.pdf",
              null,
              false,
              false,true,true,["/bin/bash","-c"]);
          
              String respText = new File(workingDir+"/output.txt").exists()?FileUtils.readFileToString(new File(workingDir+"/output.txt"),"utf-8"):null;
              resp = respText != null?exec.add("solvent_javalang_class","map").eval(respText,"json"):null;
          
              if(resp != null && resp.executionArn != null){
                  Thread.sleep(10000);
                  new File(workingDir+"/output.txt").delete();

                  resp = cliManager.CLIManagerAPI().runCLI(
                    workingDir,
                    false,
                    "chmod +x get_workflow_result.sh && ./get_workflow_result.sh ${resp.executionArn}",
                    null,
                    false,
                    false,true,true,["/bin/bash","-c"]);

                    respText = new File(workingDir+"/output.txt").exists()?FileUtils.readFileToString(new File(workingDir+"/output.txt"),"utf-8"):null;
                    resp = respText != null?exec.add("solvent_javalang_class","map").eval(respText,"json"):null;                
              }
          
          	  respMsg.content = "${resp != null && resp.body != null?("PDF text extracted. Here is the extracted pdf text:"+resp.body):(resp != null?aiImplPlugin.stringify(resp):"Unable to extract text, unknown reason")}".toString(); 
        }
        catch(Exception e){
          exec.logger().warn(e)
          respMsg.content = "Error invoking pdf extraction, ${e.getMessage()}.".toString();
        }
        
        return respMsg;
    }
  
    public Map upload_file_to_cloud_storage(Map toolCallArgs,Map tool_call,Map prompt,Map config,Map session,Object aiProviderInterface,Object aiImplPlugin){
      
		Map respMsg = [
            role:"tool",
            tool_call_id:tool_call.id,
            name:tool_call.function.name,
            content:"file successfully uploaded."
        ];
      
        if(toolCallArgs.filePath == null || toolCallArgs.filePath.isEmpty()){
            respMsg.content = "No valid file path specified";
            return respMsg;
        }
      
        String filePath = toolCallArgs.filePath;
      
        //List leads = String.class.isInstance(toolCallArgs.leads)?exec.add("solvent_javalang_class","map").eval(aiImplPlugin.jsonrepair(toolCallArgs.leads),"json"):toolCallArgs.leads;     
      
        try
        {
            Map fileInfo = aiImplPlugin.getFileInfo(prompt,tool_call,toolCallArgs,[filePath:filePath]);
            if(fileInfo.error != null){
               respMsg.content = "upload file error for file ${filePath}, ${fileInfo.error}".toString();
               return respMsg;
            }

            String workingDir = "/home/ubuntu/aws-workflow";
          	//FileUtils.writeStringToFile(new File(workingDir+"/input.txt"),instruction,"utf-8");
          
            File file = pluginContext.resolveToPlatformAssetDir(fileInfo.filePath);
            if(!file.exists()){
               respMsg.content = "upload file ${filePath} doesn't exist.".toString();
               return respMsg;
            }
          		  
            //FileUtils.copyFile(pdfFile,new File(workingDir+"/input.pdf"));
            //if(!pdfFile.getName().equals("input.pdf"))
          	//   new File(workingDir+"/"+pdfFile.getName()).renameTo(new File(workingDir+"/input.pdf"));
          
            //find plugin that handles object related functions && ./invoke_biomni.sh
            Object cliManager = moduleContext.getPlugin(aiProviderInterface,"com.codesolvent.solvent.ide.plugin.tools.cli-manager");

            Map resp = cliManager.CLIManagerAPI().runCLI(
              workingDir,
              false,
              "chmod +x upload_to_s3.sh && ./upload_to_s3.sh ${file.getCanonicalPath()}".toString(),
              null,
              false,
              false,true,true,["/bin/bash","-c"]);
          
            respMsg.content = "${(resp != null?"upload operation response:"+aiImplPlugin.stringify(resp):"file successfully uploaded.")}".toString(); 
        }
        catch(Exception e){
          exec.logger().warn(e)
          respMsg.content = "Error uploading file to cloud storage, ${e.getMessage()}.".toString();
        }
        
        return respMsg;
    }
  
  
    public Object init(Map toolImplPluginConfig,Map config,Map session,Object aiProviderInterface,Object aiImplPlugin){
       //toolDefinition = exec.call("<path to tool definitions>/toolDefs{}");
       return this;
    }
  
    public Map preparePrompt(Map prompt,Map config,Map session,Object aiProviderInterface,Object aiImplPlugin){

          if(prompt.apiRequest == null)
          	prompt.put("apiRequest",new HashMap());

          if(prompt.apiRequest.tools == null)
          	prompt.apiRequest.put("tools",new ArrayList());

          //append tools provided by this plugin
          if(prompt.toolDefinition != null && prompt.toolDefinition.defs != null)
          	 prompt.apiRequest.tools.addAll(aiImplPlugin.cloneObject(prompt.toolDefinition.defs));

          List dedupTools = new ArrayList();
          for(Map tool : prompt.apiRequest.tools){
              if(!aiImplPlugin.isInList(dedupTools,t->t.function.name.equals(tool.function.name)))
                  dedupTools.add(tool);
          }
          prompt.apiRequest.put("tools",dedupTools);

          if(prompt.apiRequest.tool_choice == null)
          	prompt.apiRequest.put("tool_choice","auto");

          return prompt;          
    }
  
    public List getTools(Map prompt,Map config,Map session,Object appBuilder,Object aiProviderInterface,Object aiImplPlugin){
      	return getTools(prompt,config,session,aiProviderInterface,aiImplPlugin);
    }
  
    public List getTools(Map prompt,Map config,Map session,Object aiProviderInterface,Object aiImplPlugin){
        return (prompt.toolDefinition != null  && prompt.toolDefinition.defs != null)?aiImplPlugin.cloneObject(prompt.toolDefinition.defs):new ArrayList();
    }
  
    public boolean isKnownAITool(Map tool_call,Map prompt,Map config,Map session,Object aiProviderInterface,Object aiImplPlugin){
        if(prompt.toolDefinition != null  && prompt.toolDefinition.defs != null){
            for(Map tool: prompt.toolDefinition.defs){
               if(tool.function.name.equals(tool_call.function.name)) 
                  return true;
            }
        }
        return false;
    }
  
    public Object executeToolCall(Map tool_call,Map prompt,Map config,Map session,Object aiProviderInterface,Object aiImplPlugin){
        Map toolCallArgs = new HashMap();
        if(tool_call.function.arguments != null)
         	toolCallArgs = String.class.isInstance(tool_call.function.arguments)?exec.add("solvent_javalang_class","map").eval(tool_call.function.arguments,"json"):tool_call.function.arguments;
      
        String executionEnvironment = prompt.executionEnvironment;
        if(toolCallArgs.executionEnvironment != null && !toolCallArgs.executionEnvironment.equals("any"))
           executionEnvironment = toolCallArgs.executionEnvironment;
          
        if(executionEnvironment != null && executionEnvironment.equals("front-end"))
          	return [sendToClient:true];      
      
      	try
        {
        	return this.getClass().getMethod(tool_call.function.name,Map.class,Map.class,Map.class,Map.class,Map.class,Object.class,Object.class).invoke(this,toolCallArgs,tool_call,prompt,config,session,aiProviderInterface,aiImplPlugin);
        } 
        catch(InvocationTargetException e) {
            throw e.getCause();
        }
    }
}

return new DocProcessor(arguments.get("httpRequest"),arguments.get("httpResponse"),arguments,exec);
