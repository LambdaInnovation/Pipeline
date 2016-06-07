A experimental modern GL rendering pipeline implementation for Minecraft mods.

# Functionalities

1. An arbitary rendering pipeline 
2. Support for effect rendering in world space
3. Support for concrete object rendering in world space
4. Support for rendering in GUI space

# Draft

- ShaderProgram

- (Generated) XXXMaterial
  -> (Generated) XXXInstanceData - Handles and stores per-instance attribute
  -> (Generated) XXXMesh - Handles and stores per-vertex attribute
  - Stores uniforms
  
- Pipeline
  - Accepts (Material, Mesh, InstanceData) combination and group drawcalls by calling flush()
  
- MCPipeline
  - gives conventional pipelines (e.g. effect rendering)

- ObjParser
