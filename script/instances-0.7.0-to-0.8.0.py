import re
import json
import glob
import argparse
import os
from copy import deepcopy


def get_template_variables(template):
      variablePattern = re.compile("\\{\\{([A-Za-z][A-Za-z0-9\\-\\_\\_]*)\\}\\}")
      variables = set(variablePattern.findall(template))
      return variables


def update_parameter_infos(paramter_infos, template_variables, instance_path):
    updated_parameter_infos = deepcopy(paramter_infos)
    for variable in (template_variables - set(parameter_infos.keys())):
        print("Adding missing variable '%s' to the parameterInfos of instance %s" % (variable, instance_path))
        updated_parameter_infos[variable] = {"id": variable}
    return updated_parameter_infos
        

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("instance_directory")

    opts = parser.parse_args()

    for instance_path in glob.glob(os.path.join(opts.instance_directory, "*.json")):
        print("Processing", instance_path)
        instance = json.loads(open(instance_path).read())
        
        assert "template" in instance
        template = instance["template"]
        assert "template" in template
        assert "parameterInfos" in template
        template_variables = get_template_variables(template["template"])
        parameter_infos = template["parameterInfos"]
        
        complete_parameter_infos = update_parameter_infos(parameter_infos, template_variables, instance_path)
        if parameter_infos != complete_parameter_infos:
            template["parameterInfos"] = complete_parameter_infos
            open(instance_path, "w+").write(json.dumps(instance))
