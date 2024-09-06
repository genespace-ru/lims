import React, {Component} from 'react';
import {registerTableBox, processHashUrl, Navs} from 'be5-react';
import {Field, FieldNotEmpty} from './utils';

class ProjectTabTableBox extends Component
{
  render()
  {
    const project = this.props.value.data.attributes.rows[0];

    this.title = project.PageTitle.value;
    be5.ui.setTitle(this.title);

    
    return (
        <div className="container">
            <Field  title='Название'            value={project.Name.value}/>
            <br/>
            <Field  title='Описание'            value={project.Description.value}/>
            <br/>
            <Field  title='Создано'             value={project.creationdate___.value}/>
            <Field  title='Создал'              value={project.whoinserted___.value}/>
            <FieldNotEmpty  title='Изменено'    value={project.whomodified___.value}/>
            <FieldNotEmpty  title='Изменил'     value={project.modificationdate___.value}/>
        </div>);
  }
}

registerTableBox('projectTab', ProjectTabTableBox);
export default ProjectTabTableBox;