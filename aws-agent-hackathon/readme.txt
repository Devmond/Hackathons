This code is an extract from the Solvent platform, it shows the specific integrations 
required to plug AWS console management into the Solvent platform.

Solvent Agents
-----------------
Edgar- A personal assistant that the user interacts with directly. Edgar uses the AWS Integration agent as an assistant.
AWS Integration assistant - A technical assistant that uses tools to interact with the AWS infrastructure.
   agents/tool-impl/DocProcessor.ste is the Groovy code (in Solvent executable format) that invokes the tools for interacting with AWS.
   agents/tool-impl/DocProcessor.groovy is just a readable extract of agents/tool-impl/DocProcessor.ste.
    
    
The Solvent plugin configuration of the above two agents is extracted into the following json files 
   agents/tool-impl/edgar-personal-assistant.json
   agents/tool-impl/aws-integration-service-assistant.json
    
test