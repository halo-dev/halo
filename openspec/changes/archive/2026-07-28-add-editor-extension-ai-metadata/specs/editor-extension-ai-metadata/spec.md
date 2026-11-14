## ADDED Requirements

### Requirement: Native Tiptap metadata declaration
The rich-text editor package SHALL expose a public `addHaloEditorMetadata()` declaration hook on native Tiptap Node, Mark, and Extension configurations without replacing their construction or registration APIs.

#### Scenario: Third-party Node declaration
- **WHEN** a plugin creates a Node with native `Node.create()` and declares `addHaloEditorMetadata()`
- **THEN** the Node SHALL remain a native Tiptap extension and its metadata SHALL be available to Manifest generation

#### Scenario: Legacy extension without metadata
- **WHEN** a registered extension does not declare `addHaloEditorMetadata()`
- **THEN** the Editor SHALL continue to initialize and the final Node or Mark SHALL be represented using schema-derived information only

#### Scenario: AI-disabled component
- **WHEN** a Node or Mark resolves to `ai: false`
- **THEN** its schema description SHALL remain in the Manifest and the explicit disabled decision SHALL be preserved

### Requirement: Self-owned component guidance
Each Node or Mark metadata declaration SHALL describe only that component's AI usage and optional placement constraints.

#### Scenario: Figure caption declaration
- **WHEN** `figureCaption` declares `allowedParents: ["figure"]`, `minPerParent: 0`, and `maxPerParent: 1`
- **THEN** the Manifest SHALL associate those advisory constraints with `figureCaption`
- **AND** the `figure` metadata SHALL NOT be required to declare or register `figureCaption`

#### Scenario: Unregistered component
- **WHEN** a component is not present in the final schema
- **THEN** no Manifest component or dangling self metadata SHALL be emitted for it

### Requirement: AI usage metadata
A final AI metadata object SHALL contain a non-empty description and MAY contain aliases, exposure, usage guidance, attribute guidance, generation guidance, and HTML examples.

#### Scenario: Default exposure
- **WHEN** a valid AI object omits `exposure`
- **THEN** the Manifest SHALL emit `exposure: "available"`

#### Scenario: Attribute guidance shorthand
- **WHEN** a declaration uses a string as an attribute guidance value
- **THEN** the Manifest SHALL normalize it to an object containing that string as `description`

#### Scenario: Enumerated attribute values
- **WHEN** an attribute accepts a known finite set of values
- **THEN** its guidance SHALL be able to declare scalar `allowedValues`

#### Scenario: Capability-dependent generation
- **WHEN** generation mode is `requires-capability`
- **THEN** the declaration SHALL include at least one open string identifier in `requiredCapabilities`
- **AND** Halo SHALL NOT resolve the identifier to a tool or execute it

#### Scenario: Automatic example
- **WHEN** a final component has a valid AI object and no declared examples
- **THEN** the resolver MAY add one schema-generated HTML example to `ai.examples`

#### Scenario: Schema-only component example
- **WHEN** a component has no AI object or declares `ai: false`
- **THEN** the resolver SHALL NOT synthesize an AI object or AI example

### Requirement: Automatic inheritance and contributions
The resolver SHALL automatically compose metadata from the final extension ancestry and directed contributions without requiring a plugin to call a parent metadata hook.

#### Scenario: Extended code block
- **WHEN** a plugin extends a Halo code block and locally declares guidance only for a new `highlightTheme` attribute
- **THEN** the final code block SHALL retain inherited AI metadata and add the new attribute guidance

#### Scenario: Configured extension
- **WHEN** `.configure()` changes the final options used by a metadata hook
- **THEN** Manifest generation SHALL invoke the hook with the final configured options

#### Scenario: Plain Extension contribution
- **WHEN** a plain Extension adds a global attribute to explicitly named final Node or Mark targets and contributes guidance
- **THEN** the guidance SHALL be merged independently into each valid target component

#### Scenario: Contribution precedence
- **WHEN** multiple contributions declare the same final field
- **THEN** higher Tiptap priority SHALL win
- **AND** later registration SHALL win at equal priority

#### Scenario: Array patch
- **WHEN** a child or higher-precedence patch explicitly declares an array field
- **THEN** that array SHALL replace the inherited array rather than append implicitly

### Requirement: Final Editor Manifest
The package SHALL synchronously derive a complete, deterministic Manifest from the live Editor's final extension set and final schema.

#### Scenario: Complete final component catalog
- **WHEN** the Manifest is generated
- **THEN** every final schema Node and Mark SHALL appear exactly once using `kind + name` identity
- **AND** nested or duplicate extension declarations SHALL NOT create duplicate Manifest components

#### Scenario: Schema field whitelist
- **WHEN** schema information is added to a component
- **THEN** only the documented Node or Mark schema fields, attributes, and serializable static HTML parsing rule fields SHALL be emitted
- **AND** functions and unknown custom NodeSpec fields SHALL be omitted

#### Scenario: Stable normalization
- **WHEN** two Editors have equivalent final schema and metadata but irrelevant extension registration ordering differs
- **THEN** their normalized Manifest content and signature SHALL be equal

#### Scenario: Meaningful change
- **WHEN** final schema or valid AI metadata changes
- **THEN** the Manifest signature SHALL change

#### Scenario: Versioned format
- **WHEN** a Manifest is generated by this contract
- **THEN** it SHALL contain numeric `version: 1`, a synchronous deterministic `signature`, and flat component records

### Requirement: Advisory fail-soft processing
Metadata declarations SHALL be treated as untrusted advisory data and SHALL NOT affect Editor runtime behavior.

#### Scenario: Throwing metadata hook
- **WHEN** a metadata hook throws
- **THEN** the Editor SHALL remain usable
- **AND** Manifest generation SHALL omit the affected declaration and warn in development

#### Scenario: Unknown metadata field
- **WHEN** a runtime declaration contains a field outside the public metadata contract
- **THEN** the unknown field SHALL be omitted from the Manifest

#### Scenario: Invalid attribute guidance
- **WHEN** guidance references an attribute not present on a target component
- **THEN** that invalid guidance SHALL be omitted while other valid metadata remains

#### Scenario: Invalid structure declaration
- **WHEN** structure references a missing parent or has inconsistent cardinality
- **THEN** the structure section SHALL be omitted while the AI and schema sections remain

#### Scenario: HTML example validation
- **WHEN** a declared HTML example cannot be parsed with the final schema
- **THEN** the example SHALL be omitted
- **AND** validation SHALL NOT require a parseable example to contain the declaring component

#### Scenario: No document enforcement
- **WHEN** metadata declares AI usage, structure, attribute values, or generation guidance
- **THEN** Halo SHALL NOT validate an editor document, intercept a transaction, or enforce an AI edit based on that metadata

### Requirement: Bounded metadata
Manifest generation SHALL bound explicit third-party AI metadata before exposing it to consumers.

#### Scenario: Field and component limits
- **WHEN** text, arrays, allowed values, examples, or component metadata exceed their documented limits
- **THEN** the excess invalid content SHALL be omitted without affecting Editor initialization

#### Scenario: Manifest metadata limit
- **WHEN** explicit AI metadata would exceed 128 KiB for one Manifest
- **THEN** Manifest generation SHALL keep schema-derived component information and deterministically omit excess explicit metadata

### Requirement: Halo built-in metadata coverage
Every Node and Mark in Halo's default `ExtensionsKit` SHALL explicitly resolve to a complete AI object or `ai: false`.

#### Scenario: Content-facing default component
- **WHEN** a default content-facing component such as a code block, table, media node, list, or link is included
- **THEN** it SHALL provide English guidance sufficient for an AI consumer to identify and use the component

#### Scenario: Internal default component
- **WHEN** a default Node or Mark is not suitable for direct AI creation or modification
- **THEN** it MAY explicitly declare `ai: false`
- **AND** it SHALL NOT remain accidentally undecided

#### Scenario: Global attribute extension
- **WHEN** a Halo plain Extension adds a user-facing global attribute
- **THEN** it SHALL contribute relevant attribute guidance to its explicitly configured target components

#### Scenario: Materially distinct built-in forms
- **WHEN** a default component has materially distinct valid forms
- **THEN** its guidance or examples SHALL identify those alternatives without requiring every attribute combination
- **AND** `figure` SHALL identify image, video, and audio children as supported forms

#### Scenario: Complete built-in attribute guidance
- **WHEN** a default content-facing Node or Mark exposes an attribute in the final schema
- **THEN** its AI metadata SHALL explain how to use that attribute or explicitly advise that generated persisted content omit the editor-managed attribute

#### Scenario: Runtime-aligned built-in guidance
- **WHEN** a built-in component is inline, is normally nested by editor commands, normalizes a legacy form, or maintains an attribute automatically
- **THEN** its description and guidance SHALL reflect that runtime behavior
- **AND** generated content SHALL be advised to omit editor-managed values

#### Scenario: Representative direct-HTML round trip
- **WHEN** a built-in example uses attributes that materially distinguish a supported form
- **THEN** parsing that HTML with the final editor schema SHALL preserve the intended component and those distinguishing attributes
- **AND** the existing three-column HTML form SHALL preserve `cols: 3`

### Requirement: Third-party integration documentation
The public editor package SHALL document how plugins declare and consume editor AI metadata.

#### Scenario: Plugin author examples
- **WHEN** a plugin developer reads the documentation
- **THEN** examples SHALL cover a new mathematical component, extension of an existing code block, a plain Extension contribution, `ai: false`, schema fallback, fail-soft limits, and Manifest generation
