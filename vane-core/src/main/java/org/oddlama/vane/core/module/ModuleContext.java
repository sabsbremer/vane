package org.oddlama.vane.core.module;

import static org.oddlama.vane.util.Util.prepend;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

import org.oddlama.vane.annotation.command.Aliases;
import org.oddlama.vane.annotation.command.Name;
import org.oddlama.vane.annotation.command.VaneCommand;
import org.oddlama.vane.annotation.lang.LangString;
import org.oddlama.vane.core.module.Module;
import org.oddlama.vane.core.command.params.AnyParam;

/**
 * A ModuleContext is an association to a specific Module and also a
 * grouping of config and language variables with a common namespace.
 */
public class ModuleContext<T extends Module<T>> implements Context<T> {
	protected Context<T> context;
	protected T module; // cache to not generate chains of get_context()
	private String namespace;
	private List<Context<T>> subcontexts = new ArrayList<>();
	private List<ModuleComponent<T>> components = new ArrayList<>();

	public ModuleContext(Context<T> context, String namespace) {
		this(context, namespace, true);
	}

	public ModuleContext(Context<T> context, String namespace, boolean compile_self) {
		this.context = context;
		this.module = context.get_module();
		this.namespace = namespace;

		if (compile_self) {
			compile_self();
		}
	}

	private String get_namespaced_variable(String variable) {
		return namespace + "_" + variable;
	}

	protected void compile_self() {
		// Compile localization and config fields
		module.lang_manager.compile(this, this::get_namespaced_variable);
		module.config_manager.compile(this, this::get_namespaced_variable);
		context.add_child(this);
	}

	@Override
	public void compile(ModuleComponent<T> component) {
		components.add(component);
		module.lang_manager.compile(component, this::get_namespaced_variable);
		module.config_manager.compile(component, this::get_namespaced_variable);
	}

	@Override
	public void add_child(Context<T> subcontext) {
		subcontexts.add(subcontext);
	}

	@Override
	public Context<T> get_context() {
		return context;
	}

	@Override
	public T get_module() {
		return module;
	}

	@Override
	public String get_namespace() {
		return namespace;
	}

	@Override
	public void enable() {
		on_enable();
		for (var component : components) {
			component.on_enable();
		}
		for (var subcontext : subcontexts) {
			subcontext.enable();
		}
	}

	@Override
	public void disable() {
		on_disable();
		for (var component : components) {
			component.on_disable();
		}
		for (var subcontext : subcontexts) {
			subcontext.disable();
		}
	}

	@Override
	public void config_change() {
		on_config_change();
		for (var component : components) {
			component.on_config_change();
		}
		for (var subcontext : subcontexts) {
			subcontext.config_change();
		}
	}
}
