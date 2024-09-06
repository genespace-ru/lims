import React, { useEffect } from 'react';
import { registerTableBox, processHashUrl, Navs } from 'be5-react';

const ProjectCardTableBox = ({ value }) => {
  const project = value.data.attributes.rows[0];
  const title = project.PageTitle.value;

  useEffect(() => {
    be5.ui.setTitle(title);
  }, [title]);

  const steps = [
    { title: "Проект",   url: "#!table/projects/ProjectTab/___prjID=" + project.ID.value },
    { title: "Запуск", url: "#!table/runs/RunTab/___prjID=" + project.ID.value },
    { title: "Образцы", url: "#!table/samples/ForProjectCard/___prjID=" + project.ID.value },
    { title: "Контроль", url: "#!table/projects/ToDo/___prjID=" + project.ID.value },
    { title: "Анализы", url: "#!table/analyses/ForProjectCard/___prjID=" + project.ID.value },
    { title: "Файлы", url: "#!table/attachments/ForProjectCard/___prjID=" + project.ID.value },
  ];

  return (
    <div className="projectInfo">
      <h1>{title}</h1>
      <Navs steps={steps} tabs startAtStep={0} />
    </div>
  );
};

registerTableBox('projectCard', ProjectCardTableBox);

export default ProjectCardTableBox;
